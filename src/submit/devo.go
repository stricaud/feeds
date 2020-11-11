package submit

import "fmt"
import "io/ioutil"
import "crypto/tls"
import "crypto/x509"
import "strings"
import "encoding/json"
import "strconv"

import "gopkg.in/ini.v1"

import "../mispobjects"
import "../storage"
import "../uuidizer"

type DevoSender struct {
	host      string
	port      string
	chain     []byte
	cert      []byte
	key       []byte
	verify    bool
	syslogmsg string
	Conn     *tls.Conn
}

func (sender *DevoSender) NewDevoSender(host string, port string, chain_file string, cert_file string, key_file string, verify bool) {
	sender.host = host
	sender.port = port

	chain, err := ioutil.ReadFile(chain_file)
	if err != nil {
		fmt.Println("Error reading chain certificate")
	}
	sender.chain = chain
	
	sender.cert, err = ioutil.ReadFile(cert_file)
	if err != nil {
		fmt.Println("Error reading chain certificate")
	}
	sender.key, err = ioutil.ReadFile(key_file)
	if err != nil {
		fmt.Println("Error reading chain certificate")
	}
	sender.verify = verify
	sender.syslogmsg = "<14>Jan  1 00:00:00 2019-USA-0042.local TAG: MSG\n"
}

func (sender *DevoSender) Connect() (*tls.Conn, error) {
	chain := x509.NewCertPool()
	err := chain.AppendCertsFromPEM(sender.chain)
	if !err {
		fmt.Println("Error Appending certificate")
	}
	
	cert, _ := tls.X509KeyPair(
		sender.cert,
		sender.key,
	)
	
	conf := &tls.Config{
		ClientAuth:   tls.RequireAndVerifyClientCert,
		Certificates: []tls.Certificate{cert},
		InsecureSkipVerify: !sender.verify,
		RootCAs: chain,
	}

	remote := sender.host + ":" + sender.port
	
	return tls.Dial("tcp", remote, conf)
}

func (sender *DevoSender) Send(conn *tls.Conn, tag string, message string) {
	msg := strings.Replace(sender.syslogmsg, "TAG", tag, 1)
	msg = strings.Replace(msg, "MSG", message, 1)

	conn.Write([]byte(msg))	
}

func BuildDevoMispFromEvent(config *ini.File, event *mispobjects.Event) []string {
	devomisp := mispobjects.DevoMispAttribute {}

	devo_event := mispobjects.Event{}
	devo_event.Id = event.Id
	devo_event.Date = event.Date
	devo_event.Info = event.Info
	devo_event.Uuid = event.Uuid
	devo_event.Published = event.Published
	devo_event.Analysis = event.Analysis
	devo_event.ThreatLevelId = event.ThreatLevelId
	devo_event.OrgId = event.OrgId
	devo_event.OrgcId = event.OrgcId
	devo_event.Distribution = event.Distribution
	devo_event.SharingGroupId = event.SharingGroupId 
	devo_event.Orgc = event.Orgc

	devomisp.Event = devo_event
	devomisp.EventTags = event.EventTag
	
	// for _, tag := range event.Tags {
	// 	eventtag := mispobjects.EventTag{}
	// 	tag_id := storage.GetTagId(config, &tag)
	// 	eventtag.Id = strconv.Itoa(tag_id)
	// 	eventtag.Name = tag.Name
	// 	eventtag.Colour = tag.Colour
	// 	eventtag.Exportable = tag.Exportable
	// 	eventtag.HideTag = false
	// 	devomisp.EventTags = append(devomisp.EventTags, eventtag)
	// }

	var events []string
	for _, attr := range event.Attributes {
		id := storage.GetAttributeId(config)
		uuidizer.AttributeUuidize(event, &attr)
		devomisp.Attribute = attr
		devomisp.Attribute.Id = strconv.FormatUint(id, 10)
		devomisp.Attribute.EventId = string(event.Id)
		devomisp_json, _ := json.Marshal(devomisp)
		events = append(events, string(devomisp_json))
	}
	
	// fmt.Println(string(devomisp_json))
	
	return events
}
