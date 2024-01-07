# 操作步骤
## 打印内存文件
```shell
pmap -X <pid> > pmap.txt
jcmd <pid> VM.native_memory detail > nmt.txt
```