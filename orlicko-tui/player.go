package main

import (
	"os/exec"
	"sync"
)

const streamURL = "https://mediaservice.radioorlicko.cz/stream192.mp3"

type player struct {
	cmd *exec.Cmd
	mu  sync.Mutex
}

func newPlayer() *player {
	return &player{}
}

func (p *player) start() error {
	p.mu.Lock()
	defer p.mu.Unlock()

	p.cmd = exec.Command("mpv", "--no-video", "--no-terminal", "--really-quiet", streamURL)
	return p.cmd.Start()
}

func (p *player) wait() error {
	p.mu.Lock()
	cmd := p.cmd
	p.mu.Unlock()

	if cmd == nil {
		return nil
	}
	return cmd.Wait()
}

func (p *player) stop() {
	p.mu.Lock()
	defer p.mu.Unlock()

	if p.cmd != nil && p.cmd.Process != nil {
		_ = p.cmd.Process.Kill()
		p.cmd = nil
	}
}
