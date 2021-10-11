# rDoS

## Disclaimer

Never use tools like this against anything you do not have permission to mess with! It could get you in big legal trouble even if you have good intentions.

Note: most bug bounty programs forbid DoS/DDoS so please double-check and ask for clarity if you are unsure.

## Introduction

This is a reverse denial of service attack of sorts. Inspired by the [slowloris](https://www.cloudflare.com/en-gb/learning/ddos/ddos-attack-tools/slowloris/) and [R.U.D.Y](https://www.cloudflare.com/en-gb/learning/ddos/ddos-attack-tools/r-u-dead-yet-rudy/) attacks, it is a super basic web server that responds painfully slow to requests. Serving a response of just 100 bytes of data can take 5+ minutes depending on `byteFlushSleepMs`.

## Attack scenarios

rDoS is painfully slow by design and no sane user would wait out the painfully slow responses so the focus is on systems not humans.

rDos has potential anywhere you can get a system to send requests to your server. For examples, webhooks. Simply register your server for webhook notifications, wait for or trigger some notifications and watch the logs to see how long the target waits.

## Getting started

1. Install docker or another tool to do `docker compose`
2. `docker compose build`
3. `docker compose up`
4. Check the logs to see test requests from JS and JVM environments being super slow

Tweak the `byteFlushSleepMs` value for SlowHttpServer to go slower (cause more chaos) or faster (avoid client read timeout).

## Mitigation

Existing security solutions, reverse-proxies (nginx) and DDoS prevention services (CloudFlare), mainly focus on ingress traffic, where the client might attack the server. For egress traffic that is vulnerable to rDos, you might want to consider the following options.


### Timeouts

One of the best mitigations to an attack like this to is to simply timeout long requests. Be careful though because read, write and connection timeouts alone are normally not enough. rDos is meant to trickle response data fast enough to avoid these timeouts but slow enough to occupy resources and block threads for a long time. Some HTTP libraries have a total timeout/deadline option which will cause a request to fail if it hasn't completed within a certain timeframe. That is the kind of timeout you want. See `callWithSuperAgentTimeout` of [./test/js/index.js](./test/js/index.js) for an example.

### Allow lists and rate limiting

If it is possible, only allow out-going requests to servers that you can reasonably trust. 

Alternatively, make it difficult for an attacker to trigger requests to their servers by putting that functionality behind a sign-up process. Most APIs that allow registering webhooks, for example, require you to create an account, get an API key, etc. Combining that with rate limiting the number of concurrent out-going requests to the same servers can minimize the impact a single attacker and server can have. An attacker could still setup multiple accounts, domains or servers to get around this but it requires more investment from them.

### Multithreading and horizontal scaling

Multithreading and horizontal scaling can make it harder for an attacker to exhaust your out-going connection capacity but it can come with a financial cost for more compute resources.

## Future work

1. take parameters from the path (e.g. /?byteFlushSleepMs=1000) of the request for more dynamic usage
2. look into refactoring the request threads and ExecutorService into something more lightweight
