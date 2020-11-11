Feeds
=====

Fetch indicators from feeds in various formats to push towards the storage of your choice.

Feeds will detect the type of feed automatically, simply provide the authentication method, the protocol, the fields one want extracted and tags attached. Each feed handle runs in its own thread.

Installing
----------

* In the top directory, run make to compile. It will result in the "feeds" binary in the build directory
* To run, simply create a feeds.ini after the one provided from doc/feeds.ini.sample and execute the program in this dir.

Configuration example
---------------------

This could be your feeds.ini file:

```
misp_url=<yourmisp_instance>
misp_key=<yourkey>
cache_storage=./feeds-cache
devo_host=collector-se.devo.io
devo_port=443
devo_chain=<chain>
devo_cert=<cert>
devo_key=<key>
send=true
log_file=feeds.log

sightingdb_host=127.0.0.1
sightingdb_port=9999
sightingdb_key=changeme
sightingdb_namespace=feeds

ssl_verify=false

[CIRCL OSINT Feed]
enabled=true
provider=CIRCL
input=Network
url=https://www.circl.lu/doc/misp/feed-osint
distribution=everybody
tags=
schedule=3600
```

Running feeds
-------------

Either run the binary, or from the source dir, execute this command:

```
$ go run main.go 
Connected
https://www.circl.lu/doc/misp/feed-osint/59d21cf5-3d14-46f2-9845-4f7d950d210f.json
https://www.circl.lu/doc/misp/feed-osint/57460863-76dc-4272-8116-4ea302de0b81.json
...
```

It will pull the different indicators from the configured feeds and push them to both Devo and your MISP instance




