## Shorty: URL Shortener Service [![CircleCI](https://circleci.com/gh/regadas/shorty.svg?style=svg)](https://circleci.com/gh/regadas/shorty)
by Filipe Regadas (@[regadas](http://twitter.com/regadas))

This service is a simple url shortener service that uses `base36` to encode URL's. Uses Google Datastore and is deployable to Google App Engine through the usage of managed vms (Flexible Environment).  

## Usage

You can run it locally by:

```
$ gcloud beta emulators datastore start
$ eval "$(gcloud beta emulators datastore env-init)" 
$ sbt shorty-gae/run
```

POST a list of url's and get there id:

```

$ curl -XPOST http://localhost:8080 --data 'url=http://google.com&url=http://reddit.com' 2>/dev/null | jq .

[
  {
      "id": "tatiji",
      "url": "http://google.com"
  },
  {
      "id": "n827h3",
      "url": "http://reddit.com"
  }
]
```

Use those id's:

```
$ curl -L http://localhost:8080/tatiji
```

## Google App Engine deploy

```
$ glcoud init 
$ sbt shorty-gae/assembly
$ gcloud preview app deploy shorty-gae/app.yaml --docker-build local --promote
```
