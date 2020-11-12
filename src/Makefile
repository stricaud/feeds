GOBUILD=go build

all: feeds

SRC=main.go feeds/detect.go feeds/feed.go feeds/feedtype.go

feeds: $(SRC)
	$(GOBUILD) -o ../build/feeds

clean:
	rm -f *~
	rm -f global/*~
	rm -f mispfetch/*~
	rm -f mispobjects/*~
	rm -f storage/*~
	rm -f submit/*~
