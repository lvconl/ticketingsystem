# ticketingsystem
## 国科大并发编程大作业
- 购票使用乐观锁实现，乐观锁认为冲突不一定会发生，即寻找符合条件的座位时不加锁，修改座位状态时加锁
- 查询满足静态一致性即可
- 退票可能会与购票重叠，所以需要加锁，使用细粒度锁，即对对应的座位加锁
