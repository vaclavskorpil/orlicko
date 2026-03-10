package main

import (
	"io"
	"net/http"
	"strings"
	"time"

	tea "github.com/charmbracelet/bubbletea"
	"github.com/charmbracelet/lipgloss"
)

const metadataURL = "https://radioorlicko.cz/data/current_song.txt"

type status int

const (
	stopped status = iota
	buffering
	playing
	errored
)

// Messages
type songUpdateMsg string
type mpvStartedMsg struct{}
type mpvExitedMsg struct{ err error }
type metadataTickMsg time.Time
type mprisReadyMsg struct{ server *mprisServer }

// Model
type model struct {
	player *player
	mpris  *mprisServer
	status status
	song   string
	errMsg string
	width  int
	height int
}

func initialModel(p *player) model {
	return model{
		player: p,
		status: stopped,
		song:   "",
	}
}

func (m model) Init() tea.Cmd {
	return fetchMetadata()
}

func (m model) Update(msg tea.Msg) (tea.Model, tea.Cmd) {
	switch msg := msg.(type) {

	case tea.WindowSizeMsg:
		m.width = msg.Width
		m.height = msg.Height

	case tea.KeyMsg:
		switch msg.String() {
		case "q", "ctrl+c":
			m.player.stop()
			m.mpris.close()
			return m, tea.Quit
		case " ":
			return m.togglePlay()
		}

	case mprisReadyMsg:
		m.mpris = msg.server

	case mprisPlayPauseMsg:
		return m.togglePlay()

	case mprisPlayMsg:
		if m.status == stopped || m.status == errored {
			return m.togglePlay()
		}

	case mprisStopMsg:
		if m.status == playing || m.status == buffering {
			m.player.stop()
			m.status = stopped
			m.mpris.updatePlaybackStatus(m.status)
		}

	case mpvStartedMsg:
		m.status = playing
		m.mpris.updatePlaybackStatus(m.status)
		return m, waitForMpv(m.player)

	case mpvExitedMsg:
		if m.status == playing || m.status == buffering {
			if msg.err != nil {
				m.status = errored
				m.errMsg = msg.err.Error()
			} else {
				m.status = stopped
			}
			m.mpris.updatePlaybackStatus(m.status)
		}

	case songUpdateMsg:
		m.song = string(msg)
		m.mpris.updateMetadata(m.song)
		return m, tickMetadata()

	case metadataTickMsg:
		return m, fetchMetadata()
	}

	return m, nil
}

func (m model) togglePlay() (tea.Model, tea.Cmd) {
	switch m.status {
	case stopped, errored:
		m.status = buffering
		m.errMsg = ""
		m.mpris.updatePlaybackStatus(m.status)
		return m, startMpv(m.player)
	case playing, buffering:
		m.player.stop()
		m.status = stopped
		m.mpris.updatePlaybackStatus(m.status)
	}
	return m, nil
}

// Commands

func startMpv(p *player) tea.Cmd {
	return func() tea.Msg {
		if err := p.start(); err != nil {
			return mpvExitedMsg{err: err}
		}
		return mpvStartedMsg{}
	}
}

func waitForMpv(p *player) tea.Cmd {
	return func() tea.Msg {
		err := p.wait()
		return mpvExitedMsg{err: err}
	}
}

func fetchMetadata() tea.Cmd {
	return func() tea.Msg {
		client := &http.Client{Timeout: 5 * time.Second}
		resp, err := client.Get(metadataURL)
		if err != nil {
			return songUpdateMsg("")
		}
		defer resp.Body.Close()
		body, err := io.ReadAll(resp.Body)
		if err != nil {
			return songUpdateMsg("")
		}
		return songUpdateMsg(strings.TrimSpace(string(body)))
	}
}

func tickMetadata() tea.Cmd {
	return tea.Tick(10*time.Second, func(t time.Time) tea.Msg {
		return metadataTickMsg(t)
	})
}

// View

var (
	titleStyle = lipgloss.NewStyle().
			Bold(true).
			Foreground(lipgloss.Color("#FF6B35"))

	songStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#FFFACD"))

	statusPlayingStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("#00FF7F"))

	statusStoppedStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("#888888"))

	statusBufferingStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("#FFD700"))

	statusErrorStyle = lipgloss.NewStyle().
				Foreground(lipgloss.Color("#FF4444"))

	helpStyle = lipgloss.NewStyle().
			Foreground(lipgloss.Color("#666666"))

	boxStyle = lipgloss.NewStyle().
			Border(lipgloss.RoundedBorder()).
			BorderForeground(lipgloss.Color("#FF6B35")).
			Padding(1, 3).
			Width(40)
)

func (m model) View() string {
	title := titleStyle.Render("📻  Radio Orlicko")

	var songLine string
	if m.song != "" {
		songText := m.song
		// Box width (40) minus padding (3*2) minus border (1*2) minus "♪  " prefix (3) = 30
		maxLen := 40 - 6 - 2 - 3
		if len(songText) > maxLen {
			songText = songText[:maxLen-1] + "…"
		}
		songLine = songStyle.Render("♪  " + songText)
	} else {
		songLine = songStyle.Render("♪  ---")
	}

	var statusLine string
	switch m.status {
	case playing:
		statusLine = statusPlayingStyle.Render("▶  Playing")
	case buffering:
		statusLine = statusBufferingStyle.Render("◌  Buffering...")
	case stopped:
		statusLine = statusStoppedStyle.Render("■  Stopped")
	case errored:
		statusLine = statusErrorStyle.Render("✗  Error: " + m.errMsg)
	}

	help := helpStyle.Render("space play/stop  q quit")

	content := lipgloss.JoinVertical(lipgloss.Left,
		title,
		"",
		songLine,
		"",
		statusLine,
		"",
		help,
	)

	box := boxStyle.Render(content)

	// Center in terminal
	return lipgloss.Place(m.width, m.height, lipgloss.Center, lipgloss.Center, box)
}
