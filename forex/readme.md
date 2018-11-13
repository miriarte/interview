Forex
===============
This is a simple application to fetch an exchange rate between 2 currencies. There are some design choices that might 
seem weird but are quite OK given the restrictions:
- 5 minutes exchange rate max age
- 1forge free api limit is 1k/day
- Api should support 10k calls/day. This means 417 calls/hour and 35 calls/(5min).
- we offer a limited set of currencies: 8 currencies + USD, which is good!

So my decisions:

* Fetch quotes from all direct combinations of currencies(USD,*) in just 1 call; This gives us:
  * a max daily number of calls of 288 (12 calls per hour * 24 hours a day)
  * wouldn't be a problem given that the URL size is below the 255 char limit (7 chars * 8 combinations = 56 chars just 
  for pairs)
* Cache them with 5 min expiration(configurable)
* Build all rates using USD as pivot. As 1forge does a quotation using only 1 location (a UK broker), the risk of 
arbitrage and conversion delay is reduced. So I could give to API some room to fetch most updated direct calls given 
some business decision

How to use
=============
- insert yours 1forge API credentials on `resources.conf` file.
- ```./sbt `;test;it:test` ```
- `./sbt run`
- `curl -X GET 'http://localhost:8888/?from={CURR1}&to={CURR2}'`
   
Future work
===============
- Use `redis` as cache backend. This way we could scale this API app sharing the same cache values without compromising 
key limit
- better tests
- Dockerize the test and deployment