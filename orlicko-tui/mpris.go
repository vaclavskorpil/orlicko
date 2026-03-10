package main

import (
	_ "embed"
	"fmt"
	"os"
	"path/filepath"
	"strings"
	"sync"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/godbus/dbus/v5"
	"github.com/godbus/dbus/v5/introspect"
)

//go:embed logo.webp
var logoData []byte

const (
	mprisPath    = "/org/mpris/MediaPlayer2"
	mprisBusName = "org.mpris.MediaPlayer2.orlicko"
	mprisIface   = "org.mpris.MediaPlayer2"
	playerIface  = "org.mpris.MediaPlayer2.Player"
	propsIface   = "org.freedesktop.DBus.Properties"
)

// Messages sent from MPRIS D-Bus calls into the bubbletea event loop.
type mprisPlayMsg struct{}
type mprisStopMsg struct{}
type mprisPlayPauseMsg struct{}

type mprisServer struct {
	conn    *dbus.Conn
	program *tea.Program
	artURL  string

	mu       sync.RWMutex
	pbStatus string
	metadata map[string]dbus.Variant
}

func newMprisServer(program *tea.Program) *mprisServer {
	conn, err := dbus.ConnectSessionBus()
	if err != nil {
		return nil
	}

	reply, err := conn.RequestName(mprisBusName, dbus.NameFlagDoNotQueue)
	if err != nil || reply != dbus.RequestNameReplyPrimaryOwner {
		conn.Close()
		return nil
	}

	s := &mprisServer{
		conn:     conn,
		program:  program,
		artURL:   cacheLogoFile(),
		pbStatus: "Stopped",
		metadata: map[string]dbus.Variant{
			"mpris:trackid": dbus.MakeVariant(dbus.ObjectPath("/org/mpris/MediaPlayer2/orlicko/notrack")),
		},
	}

	conn.Export(s, mprisPath, mprisIface)
	conn.Export(s, mprisPath, playerIface)
	conn.Export(s, mprisPath, propsIface)

	conn.Export(introspect.NewIntrospectable(&introspect.Node{
		Name: mprisPath,
		Interfaces: []introspect.Interface{
			introspect.IntrospectData,
			{
				Name: mprisIface,
				Methods: []introspect.Method{
					{Name: "Raise"},
					{Name: "Quit"},
				},
				Properties: []introspect.Property{
					{Name: "CanQuit", Type: "b", Access: "read"},
					{Name: "CanRaise", Type: "b", Access: "read"},
					{Name: "HasTrackList", Type: "b", Access: "read"},
					{Name: "Identity", Type: "s", Access: "read"},
					{Name: "SupportedUriSchemes", Type: "as", Access: "read"},
					{Name: "SupportedMimeTypes", Type: "as", Access: "read"},
				},
			},
			{
				Name: playerIface,
				Methods: []introspect.Method{
					{Name: "PlayPause"},
					{Name: "Play"},
					{Name: "Pause"},
					{Name: "Stop"},
					{Name: "Next"},
					{Name: "Previous"},
				},
				Properties: []introspect.Property{
					{Name: "PlaybackStatus", Type: "s", Access: "read"},
					{Name: "Metadata", Type: "a{sv}", Access: "read"},
					{Name: "CanGoNext", Type: "b", Access: "read"},
					{Name: "CanGoPrevious", Type: "b", Access: "read"},
					{Name: "CanPlay", Type: "b", Access: "read"},
					{Name: "CanPause", Type: "b", Access: "read"},
					{Name: "CanControl", Type: "b", Access: "read"},
					{Name: "CanSeek", Type: "b", Access: "read"},
				},
			},
		},
	}), mprisPath, "org.freedesktop.DBus.Introspectable")

	return s
}

// org.mpris.MediaPlayer2 methods

func (s *mprisServer) Raise() *dbus.Error { return nil }
func (s *mprisServer) Quit() *dbus.Error  { return nil }

// org.mpris.MediaPlayer2.Player methods

func (s *mprisServer) Play() *dbus.Error {
	s.program.Send(mprisPlayMsg{})
	return nil
}

func (s *mprisServer) Pause() *dbus.Error {
	s.program.Send(mprisStopMsg{})
	return nil
}

func (s *mprisServer) PlayPause() *dbus.Error {
	s.program.Send(mprisPlayPauseMsg{})
	return nil
}

func (s *mprisServer) Stop() *dbus.Error {
	s.program.Send(mprisStopMsg{})
	return nil
}

func (s *mprisServer) Next() *dbus.Error     { return nil }
func (s *mprisServer) Previous() *dbus.Error  { return nil }

// org.freedesktop.DBus.Properties implementation

func (s *mprisServer) Get(iface, property string) (dbus.Variant, *dbus.Error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	switch iface {
	case mprisIface:
		switch property {
		case "CanQuit":
			return dbus.MakeVariant(false), nil
		case "CanRaise":
			return dbus.MakeVariant(false), nil
		case "HasTrackList":
			return dbus.MakeVariant(false), nil
		case "Identity":
			return dbus.MakeVariant("Radio Orlicko"), nil
		case "SupportedUriSchemes":
			return dbus.MakeVariant([]string{}), nil
		case "SupportedMimeTypes":
			return dbus.MakeVariant([]string{}), nil
		}
	case playerIface:
		switch property {
		case "PlaybackStatus":
			return dbus.MakeVariant(s.pbStatus), nil
		case "Metadata":
			return dbus.MakeVariant(s.metadata), nil
		case "CanGoNext":
			return dbus.MakeVariant(false), nil
		case "CanGoPrevious":
			return dbus.MakeVariant(false), nil
		case "CanPlay":
			return dbus.MakeVariant(true), nil
		case "CanPause":
			return dbus.MakeVariant(true), nil
		case "CanControl":
			return dbus.MakeVariant(true), nil
		case "CanSeek":
			return dbus.MakeVariant(false), nil
		}
	}

	return dbus.MakeVariant(""), dbus.MakeFailedError(fmt.Errorf("unknown property %s.%s", iface, property))
}

func (s *mprisServer) GetAll(iface string) (map[string]dbus.Variant, *dbus.Error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	switch iface {
	case mprisIface:
		return map[string]dbus.Variant{
			"CanQuit":             dbus.MakeVariant(false),
			"CanRaise":            dbus.MakeVariant(false),
			"HasTrackList":        dbus.MakeVariant(false),
			"Identity":            dbus.MakeVariant("Radio Orlicko"),
			"SupportedUriSchemes": dbus.MakeVariant([]string{}),
			"SupportedMimeTypes":  dbus.MakeVariant([]string{}),
		}, nil
	case playerIface:
		return map[string]dbus.Variant{
			"PlaybackStatus": dbus.MakeVariant(s.pbStatus),
			"Metadata":       dbus.MakeVariant(s.metadata),
			"CanGoNext":      dbus.MakeVariant(false),
			"CanGoPrevious":  dbus.MakeVariant(false),
			"CanPlay":        dbus.MakeVariant(true),
			"CanPause":       dbus.MakeVariant(true),
			"CanControl":     dbus.MakeVariant(true),
			"CanSeek":        dbus.MakeVariant(false),
		}, nil
	}

	return nil, nil
}

func (s *mprisServer) Set(iface, property string, value dbus.Variant) *dbus.Error {
	return dbus.MakeFailedError(fmt.Errorf("read-only"))
}

// State update methods called from the bubbletea model

func (s *mprisServer) updatePlaybackStatus(st status) {
	if s == nil {
		return
	}

	var val string
	switch st {
	case playing, buffering:
		val = "Playing"
	default:
		val = "Stopped"
	}

	s.mu.Lock()
	s.pbStatus = val
	s.mu.Unlock()

	s.emitChanged(playerIface, map[string]dbus.Variant{
		"PlaybackStatus": dbus.MakeVariant(val),
	})
}

func (s *mprisServer) updateMetadata(song string) {
	if s == nil {
		return
	}

	metadata := map[string]dbus.Variant{
		"mpris:trackid": dbus.MakeVariant(dbus.ObjectPath("/org/mpris/MediaPlayer2/orlicko/current")),
	}
	if s.artURL != "" {
		metadata["mpris:artUrl"] = dbus.MakeVariant(s.artURL)
	}
	if song != "" {
		parts := strings.SplitN(song, " - ", 2)
		if len(parts) == 2 {
			metadata["xesam:artist"] = dbus.MakeVariant([]string{strings.TrimSpace(parts[0])})
			metadata["xesam:title"] = dbus.MakeVariant(strings.TrimSpace(parts[1]))
		} else {
			metadata["xesam:title"] = dbus.MakeVariant(song)
		}
	}

	s.mu.Lock()
	s.metadata = metadata
	s.mu.Unlock()

	s.emitChanged(playerIface, map[string]dbus.Variant{
		"Metadata": dbus.MakeVariant(metadata),
	})
}

func (s *mprisServer) emitChanged(iface string, changed map[string]dbus.Variant) {
	_ = s.conn.Emit(dbus.ObjectPath(mprisPath),
		"org.freedesktop.DBus.Properties.PropertiesChanged",
		iface, changed, []string{})
}

func (s *mprisServer) close() {
	if s == nil {
		return
	}
	s.conn.Close()
}

// cacheLogoFile writes the embedded logo to a cache directory and returns
// a file:// URI for use as mpris:artUrl. Returns "" on any failure.
func cacheLogoFile() string {
	cacheDir, err := os.UserCacheDir()
	if err != nil {
		return ""
	}
	dir := filepath.Join(cacheDir, "orlicko-tui")
	if err := os.MkdirAll(dir, 0o755); err != nil {
		return ""
	}
	path := filepath.Join(dir, "logo.webp")
	if err := os.WriteFile(path, logoData, 0o644); err != nil {
		return ""
	}
	return "file://" + path
}
