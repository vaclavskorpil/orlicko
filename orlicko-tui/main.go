package main

import (
	"fmt"
	"os"
	"os/exec"

	tea "github.com/charmbracelet/bubbletea"
)

func main() {
	if _, err := exec.LookPath("mpv"); err != nil {
		fmt.Fprintln(os.Stderr, "Error: mpv is not installed or not in PATH.")
		fmt.Fprintln(os.Stderr, "Install it with: sudo apt install mpv")
		os.Exit(1)
	}

	p := newPlayer()
	m := initialModel(p)

	program := tea.NewProgram(m, tea.WithAltScreen())

	// Set up MPRIS D-Bus integration (non-fatal if D-Bus unavailable)
	mpris := newMprisServer(program)
	defer mpris.close()
	go program.Send(mprisReadyMsg{server: mpris})

	if _, err := program.Run(); err != nil {
		fmt.Fprintln(os.Stderr, "Error running program:", err)
		os.Exit(1)
	}
}
