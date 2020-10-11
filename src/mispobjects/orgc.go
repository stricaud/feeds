package mispobjects

type Orgc struct {
	Uuid string `json:"uuid"`
	Id string `json:"id"`
	Name string `json:"name"`
	Local bool `json:"local,omitempty"`
}
