package sightingdb

import "fmt"
import "encoding/json"
import "net/http"
import "io/ioutil"
import "bytes"
import "os"
import "crypto/tls"

type SightingDB struct {
	host   string
	port   string
	key    string
	prefix string
}

type SightingData struct {
	Namespace string `json:"namespace"`
	Value     string `json:"value"`
}

type SightingDataItems struct {
	Items []SightingData `json:"items"`
}

func (sdb *SightingDB) New(host string, port string, key string, prefix string) {
	sdb.host   = host
	sdb.port   = port
	sdb.key    = key
	sdb.prefix = prefix
}

func (sdb *SightingDB) Submit(data *SightingDataItems) {

	sdb_json, _ := json.Marshal(data)
	// fmt.Println(string(sdb_json))

	tls_config := tls.Config{}
	tls_config.InsecureSkipVerify = true
	
	sdb_client := &http.Client{Transport: &http.Transport{TLSClientConfig: &tls_config}}

	sdburl := "https://" + sdb.host + ":" + sdb.port + "/wb"
	
	req, err := http.NewRequest("POST",sdburl, bytes.NewBuffer(sdb_json))
	if err != nil {
                fmt.Println("Error with request:", err)
        }
	
	req.Header.Set("Content-Type", "application/json")
	req.Header.Set("Authorization", sdb.key)
	resp, err := sdb_client.Do(req)
	if err != nil {
                fmt.Println("Error with response:", err)
		os.Exit(1)
        }
        defer resp.Body.Close()
	
        body, err := ioutil.ReadAll(resp.Body)
	if err != nil {
                fmt.Println("Error with response!")
        }

        fmt.Println(string(body))
	// for i := 0; i < len(data.Items); i++ {
	// 	fmt.Println("+")
	// 	fmt.Println(data.Items[i].Namespace)
	// 	fmt.Println(data.Items[i].Value)
	// }
}

func (sdb *SightingDB) GetPrefix() string {
	return sdb.prefix
}

func CreateDataObject() *SightingDataItems {
	data := SightingDataItems{}
	return &data
}

func AppendDataObject(sdb *SightingDB, dataitems *SightingDataItems, namespace string, value string) {
	prefixed_namespace := sdb.prefix + "/" + namespace
	data := SightingData{prefixed_namespace, value}
	dataitems.Items = append(dataitems.Items, data)
}

func ClearDataObject(dataitems *SightingDataItems) {
	dataitems.Items = nil
}
