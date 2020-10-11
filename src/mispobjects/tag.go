package mispobjects

type Tag struct {
	Id             string `json:"id"`
	Colour         string `json:"colour"`
	Name           string `json:"name"`
	Exportable     bool   `json:"exportable,omitempty"`
	OrgId          string `json:"org_id,omitempty"`
	UserId         string `json:"user_id,omitempty"`
	HideTag        bool   `json:"hide_tag,omitempty"`
	NumericalValue string `json:"numerical_value,omitempty"`
	Count          int    `json:"count,omitempty"`
	AttributeCount int    `json:"attribute_count,omitempty"`
	Favourite      bool   `json:"favourite,omitempty"`
}

type TagForJson struct {
	Tags []Tag `json:"Tag"`
}



