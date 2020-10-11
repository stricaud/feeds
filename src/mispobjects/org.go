package mispobjects

type Org struct {
	Id string `json:"id"`
	Uuid string `json:"uuid"`
	Name string `json:"name"`
	Description string `json:"description,omitempty"`
	Type string `json:"type,omitempty"`
	Sector string `json:"sector,omitempty"`
	CreatedBy string `json:"created_by,omitempty"`
	CreatedByEmail string `json:"created_by_email,omitempty"`
	RestrictedToDomain []string `json:"restricted_to_domain,omitempty"`
	DateModified string `json:"date_modified,omitempty"`
	DateCreated string `json:"date_created,omitempty"`
	Nationality string `json:"nationality,omitempty"`
	UserCount string `json:"user_count"`
	Local bool `json:"local,omitempty"`
	Contacts string `json:"contacts,omitempty"`
	LandingPage string `json:"landing_page,omitempty"`
}

type OrgJsonNamed struct {
	Organisations Org `json:"Organisation"`
}
