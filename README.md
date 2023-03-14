## What's this about?
This app is to test how different thread pools in java deal with heavy multithreaded DB load

## What I learned
- H2 is a single-threaded DB: https://dba.stackexchange.com/questions/2918/about-single-threaded-versus-multithreaded-databases-performance,
so it was not very good for testing multithreading.
- threads blocking in calls to native methods appear in the JVM as RUNNABLE, and hence are reported by
VisualVM as Running (and as consuming 100% CPU).
- Batch sql in scalikejdbc just makes a lot of `INSERT`S inside a single transaction. `Insert...multipleValues` actually
makes one `INSERT` statement with 1000 rows. This is about twice as slow as a lot of individual inserts. However,
with this approach the results resemble Robert's

## Results
These are from running insert 1000 rows and deleting all of the data for 500 times to postgresql running locally

| ThreadPool |  Run1 time | Run2 time |
|------------|------------|-----------|
| Fixed(2)   |  20.28     | 19.098    |
| Fixed(4)   | 12.795     | 12.04     |
| Fixed(8)   | 9.566      |           |
| Fixed(16)  | 10.576     | 9.922     |
| Cached     | 10.954     |           |
| ForkJoin   |9.469       | 9.397     |

Cached threadpool exhausted the connection pool: `Cannot get a connection, pool error Timeout waiting for idle object`