package mispobjects

type Response struct {
	Event Event `json:"Event"`
}

type MispResponse struct {
	Response []Response `json:"response"`
}

