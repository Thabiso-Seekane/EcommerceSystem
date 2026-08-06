# SOLUTION.md — Design Decisions and Trade-offs

## Why JPA?

JPA with Hibernate abstracts repetitive CRUD boilerplate. Entities map directly to database tables, and relationships (`@OneToMany`, `@ManyToOne`) are expressed in Java, keeping the domain model readable. Spring Data JPA repositories (`JpaRepository`) provide `findById`, `save`, `findAll`, and pagination for free — reducing the amount of SQL that needs to be written for standard operations.

---

## Why PostgreSQL?

PostgreSQL was chosen over H2 because:
- It is production-grade and supports `BIGSERIAL`, proper decimal precision, and native SQL features used in reporting
- It enforces foreign key constraints at the database level, not just the ORM level
- The reporting query uses `BETWEEN`, `GROUP BY`, and `LIMIT` — all standard SQL that runs identically in PostgreSQL and production environments
- H2 is not a substitute for integration testing real database behaviour

---

## Why DTOs?

Entities are never returned directly from controllers. DTOs:
- Decouple the API contract from the database schema (changing a column name doesn't break clients)
- Prevent accidental serialisation of lazy-loaded proxies, which causes `LazyInitializationException`
- Allow the response shape to be independent of the entity shape (e.g. `OrderItemResponse` includes `productName` denormalised for convenience)
- Enable input validation via `@NotBlank`, `@Positive` on request DTOs without polluting entities

---

## Why @Transactional?

`placeOrder` and `cancelOrder` are both `@Transactional` because they:
- Read multiple entities, apply business rules, and write back changes in a single atomic unit
- If stock deduction succeeds but order save fails, the transaction rolls back — preventing phantom stock reductions
- `cancelOrder` restores stock and changes status — both must succeed or both must fail

Read-only operations use `@Transactional(readOnly = true)` to signal to Hibernate that no dirty-checking is needed, reducing overhead.

---

## SQL Design

Three tables with normalised relationships:

```
products    1 ----< order_items >---- 1    orders
```

- `products` holds the catalogue and stock level
- `orders` holds the aggregate status and total
- `order_items` is the join table with denormalised `price` and `subtotal` (price at time of order, not current price)

The reporting query uses **native SQL** rather than JPQL because:
- `SUM`, `GROUP BY`, `ORDER BY`, `LIMIT` with aggregate filtering is cleaner in SQL
- JPQL does not support `LIMIT` directly in the same way
- Native queries can be optimised with indexes independently of the ORM

---

## Trade-offs

| Decision | Trade-off |
|---|---|
| No authentication/authorisation | Simplifies the scope; in production, Spring Security with JWT would be added |
| `ddl-auto: none` with `schema.sql` | Full control over DDL; Hibernate auto-generation is unpredictable with complex constraints |
| Price stored on `OrderItem` | Snapshot pricing at order time; product price changes don't retroactively alter order history |
| PostgreSQL for tests | Tests require a running database; Testcontainers would make this self-contained in CI |
| Single `OrderStatus` enum | Only `NEW` and `CANCELLED`; a real system would add `PROCESSING`, `SHIPPED`, `DELIVERED` |

---

## Future Improvements

- Add Spring Security with JWT authentication
- Add Testcontainers for CI-independent integration tests
- Add `PROCESSING`, `SHIPPED`, `DELIVERED` order statuses with state machine validation
- Add product image upload support
- Add pagination to the orders list endpoint
- Add OpenAPI/Swagger documentation via `springdoc-openapi`
- Add caching on product catalogue with Spring Cache + Redis
- Extract reporting into a separate read model (CQRS pattern)
