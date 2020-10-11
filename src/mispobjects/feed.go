package mispobjects

type Feed struct {
	Id string `json:"id"`
	Name string `json:"name"`
	Provider string `json:"provider"`
	Url string `json:"url"`
	Rules Rules `json:"rules"`
	Enabled bool `json:"enabled"`
	Distribution string `json:"distribution"`
	SharingGroupId string `json:"sharing_group_id"`
	TagId string `json:"tag_id"`
	Default bool `json:"default"`
	SourceFormat string `json:"source_format"`
	FixedEvent bool `json:"fixed_event"`
	DeltaMerge bool `json:"delta_merge"`
	EventId string `json:"event_id"`
	Publish bool `json:"publish"`
	OverrideIds bool `json:"override_ids"`
	Settings string `json:"settings"`
	InputSource string `json:"input_source"`
	DeleteLocalFile bool `json:"delete_local_file"`
	LookupVisible bool `json:"lookup_visible"`
	Headers string `json:"headers"`
	CachingEnabled bool `json:"caching_enabled"`
	ForceToIds bool `json:"force_to_ids"`
	OrgcId string `json:"orgc_id"`
	CacheTimestamp bool `json:"cache_timestamp"`
}
