package mispobjects

type ManifestEvent struct {
	Info               string `json:"info"`
	Orgc               Orgc   `json:"Orgc"`
	Analysis           string `json:"analysis"`
	Tags               []Tag  `json:"Tag"`
	PublishedTimestamp string `json:"published_timestamp"`
	Timestamp          string `json:"timestamp"`
	Date               string `json:"date"`
	ThreatLevelId      string `json:"threat_level_id"`	
}
