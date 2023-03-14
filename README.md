## What's this about?
This app is to test how different thread pools in Java deal with heavy concurrent database load.

## What I learned
- H2 is a single-threaded DB: https://dba.stackexchange.com/questions/2918/about-single-threaded-versus-multithreaded-databases-performance,
so it was not very good for testing multithreading.
- threads blocking in calls to native methods appear in the JVM as RUNNABLE, and hence are reported by
VisualVM as Running (and as consuming 100% CPU).
- Batch SQL in scalikejdbc just makes a lot of `INSERT`s inside a single transaction. `Insert...multipleValues` actually
makes one `INSERT` statement with 1000 rows. This is about twice as slow as a 1000 of individual inserts. However,
with this approach the results resemble Robert's.

## Results
These are from running insert 1000 rows and deleting all the data for 500 times to PostgreSQL running locally.

| ThreadPool | Run1 time | Run2 time |
|------------|-----------|-----------|
| Fixed(2)   | 20.28     | 19.098    |
| Fixed(4)   | 12.795    | 12.04     |
| Fixed(8)   | 9.566     |           |
| Fixed(16)  | 10.576    | 9.922     |
| Cached     | 10.954    |           |
| ForkJoin   | 9.469     | 9.397     |

Cached thread pool exhausted the connection pool: `Cannot get a connection, pool error Timeout waiting for idle object`

## Conclusions
It makes sense to pick a fixed thread pool with 8 threads (little performance difference between 8 and 16 threads) or a fork/join pool for
heavy database load in local runs.
Cached thread pool exhausts the JDBC connection pool causing many exceptions and fewer queries tried than necessary. 
Fixed thread pool with more threads would just occupy them without speeding up the application. 
Fixed thread pool with fewer threads would be slower.