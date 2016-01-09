## Shorty: URL Shortener Service
by Filipe Regadas (@[regadas](http://twitter.com/regadas))

This service encodes URL in base-36 and stores them in cache. 

## Usage

You can run it locally by:

```
$ sbt run
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

