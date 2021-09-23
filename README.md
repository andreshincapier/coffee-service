# Coffee service

Web application to show webflux and reactive mongo repository in action

## Find all coffees

Use to find all coffees

```bash
curl http://localhost:8080/coffees
```

## Find by coffeeId

find specific coffee by id

```bash
curl http://localhost:8080/coffees/{coffeeId}
```

## Find all orders by coffeeId

find all orders by coffeeid

```bash
curl -S http://localhost:8080/coffees/{coffeeId}/orders
```