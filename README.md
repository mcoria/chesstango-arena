# ChessTango Tools Project

## Overview

ChessTango Arena is a distributed chess engine testing and tournament management system designed to automate chess
engine evaluation through large-scale match execution. The project provides a comprehensive framework for running
tournaments, comparing engine strengths, and analyzing chess engine performance across multiple UCI-compliant engines.

The system is built using Java and Maven, with web monitoring capabilities. It leverages a distributed master-worker 
architecture using RabbitMQ for message queuing, enabling horizontal scalability through Kubernetes deployments. 
Workers can be deployed across multiple nodes to execute chess matches in parallel, while the master coordinates match 
distribution and collects results.

Key features include:

- **UCI Protocol Compliance**: Full support for the Universal Chess Interface protocol, allowing integration with any
  UCI-compliant chess engine
- **Distributed Match Execution**: RabbitMQ-based job queue system for distributing matches across multiple worker nodes
- **Scalable Architecture**: Kubernetes-ready deployment with configurable worker replicas (default 15 workers)
- **Tournament Management**: Support for round-robin tournaments, matches, and custom game configurations
- **Opening Books & Endgame Tablebases**: Integration with opening book libraries and Syzygy endgame tablebases
- **Real-time Monitoring**: Web-based ArenaTV interface for watching ongoing matches and tournaments
- **Result Persistence**: Automated storage of match results in PGN format for later analysis

## Project Structure
- **uci-gui**: GUI to engine abstraction layer. It can control both ChessTango or another UCI complaint engine.
- **uci-proxy**: Proxy for UCI complaint engines.
- **uci-arena**: Arena for playing matches or tournaments with UCI engines.
- **uci-arena-ipc**: Inter-process communication for UCI arena.
- **uci-arena-web**: Monitor for watching matches or tournaments.

# Credits
- [chessboard.js](https://chessboardjs.com) has been used for implementing ArenaTV UI

