package feeds

type FeedType int

const (
	Misp FeedType = iota
	MispResponseJson
	Csv
	FreeText
	Json
	NotFound
)

func (ft FeedType) String() string {
	return [...]string{"Misp","MispResponseJson","Csv","FreeText","Json","NotFound"}[ft]
}

