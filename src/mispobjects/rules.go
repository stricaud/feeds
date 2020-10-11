package mispobjects

type Rules struct {
	Tags RulesOrNot  `json:"tags"`
	Orgs RulesOrNot  `json:"orgs"`
	UrlParams string `json:"org_params"`
}

type RulesOrNot struct {
	Or  []string `json:"OR"`
	Not []string `json:"NOT"`
}
