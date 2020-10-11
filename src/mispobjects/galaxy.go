package mispobjects

type GalaxyCluster struct {
	Id string `json:"id"`
	Uuid string `json:"uuid"`
	CollectionUuid string `json:"collection_uuid"`
	Type string `json:"type"`
	Value string `json:"value"`
	TagName string `json:"tag_name"`
	Description string `json:"description"`
	GalaxyId string `json:"galaxy_id"`
	Source string `json:"source"`
	Authors []string `json:"authors"`
	Version string `json:"version"`
	TagId string `json:"tag_id"`
	Local bool `json:"local"`
	Meta GalaxyClusterMeta `json:"meta"`
	Galaxy Galaxy `json:"Galaxy"`
}

type GalaxyClusterMeta struct {
	GalaxyClusterMetaRefs     []string `json:"refs"`
	GalaxyClusterMetaSynonyms []string `json:"synonyms"`
	GalaxyClusterMetaType     []string `json:"type"`
}

type Galaxy struct {
	Id string `json:"id"`
	Uuid string `json:"uuid"`
	Name string `json:"name"`
	Type string `json:"type"`
	Description string `json:"description"`
	Version string `json:"version"`
	Icon string `json:"icon"`
	Namespace string `json:"namespace"`
	KillChainOrder string `json:"kill_chain_order,omitempty"`
	GalaxyCluster []GalaxyCluster `json:"GalaxyCluster"`
}
