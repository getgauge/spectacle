// Copyright 2015 ThoughtWorks, Inc.

// This file is part of getgauge/html-report.

// getgauge/html-report is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

// getgauge/html-report is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with getgauge/html-report.  If not, see <http://www.gnu.org/licenses/>.

package processor

import (
	"bytes"
	"fmt"
	"net"
	"time"

	"github.com/getgauge/spectacle/gauge_messages"
	"github.com/golang/protobuf/proto"
)

type MessageProcessor struct {
	Connection net.Conn
}

func NewMessageProcessor(host string, port string) (*MessageProcessor, error) {
	conn, err := net.Dial("tcp", fmt.Sprintf("%s:%s", host, port))
	if err == nil {
		return &MessageProcessor{Connection: conn}, nil
	} else {
		return nil, err
	}
}

func (p *MessageProcessor) GetSpecs() (*gauge_messages.APIMessage, error) {
	id := time.Now().UnixNano()
	mType := gauge_messages.APIMessage_GetAllSpecsRequest
	message := &gauge_messages.APIMessage{AllSpecsRequest: &gauge_messages.GetAllSpecsRequest{}, MessageType: &mType, MessageId: &id}
	data, err := proto.Marshal(message)
	if err != nil {
		return nil, err
	}
	responseBytes, err := p.writeDataAndGetResponse(data)
	if err != nil {
		return nil, err
	}
	responseMessage := &gauge_messages.APIMessage{}
	if err := proto.Unmarshal(responseBytes, responseMessage); err != nil {
		return nil, err
	}
	return responseMessage, nil
}

func (p *MessageProcessor) writeDataAndGetResponse(messageBytes []byte) ([]byte, error) {
	if err := p.write(messageBytes); err != nil {
		return nil, err
	}

	return p.readResponse()
}

func (p *MessageProcessor) write(messageBytes []byte) error {
	messageLen := proto.EncodeVarint(uint64(len(messageBytes)))
	data := append(messageLen, messageBytes...)
	_, err := p.Connection.Write(data)
	return err
}

func (p *MessageProcessor) readResponse() ([]byte, error) {
	buffer := new(bytes.Buffer)
	data := make([]byte, 8192)
	for {
		n, err := p.Connection.Read(data)
		if err != nil {
			p.Connection.Close()
			return nil, fmt.Errorf("Connection closed [%s] cause: %s", p.Connection.RemoteAddr(), err.Error())
		}

		buffer.Write(data[0:n])

		messageLength, bytesRead := proto.DecodeVarint(buffer.Bytes())
		if messageLength > 0 && messageLength < uint64(buffer.Len()) {
			return buffer.Bytes()[bytesRead : messageLength+uint64(bytesRead)], nil
		}
	}
}
