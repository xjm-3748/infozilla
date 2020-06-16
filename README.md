请往下拉看第三版本的算法说明！

使用方法：
1. 调用InteractClass实现的ArrayList<String> getSimilarTopTen(String bugReportPath, String projectPath)方法，
   方法返回相似度最高的10个源文件绝对路径，已染色文件都在result/colored_class文件夹下，对bugReport逐行辨别染色后的文件为bugReport同目录下的同名.marked.txt文件，
   如示例中染色后的bugReport为report1.marked.txt   已染色文件的目录修改在代码InteractClass.java的396行可以修改
   注意事项：
      1. bugReport需要为txt格式
      2. 如果返回的arrayList为空或者size=0，说明bugReport和所有文件的相似度都较低，建议用户重新输入bugReport
      3. 运行算法大约需要3-4秒，所以建议前端做一个过渡动画来降低用户的等待感。
2. 调用InteractClass实现的getColoredReport(String bugReportPath)方法可以得到对bugReport染色后的文件内容，返回String
    注意事项：
      1. 这个方法不能作为getSimilarTopTen方法的前置方法

运行后的生成物：
report1.code.txt为提取出来的源代码部分，report1.txt.cleaned为提取出来的自然语言部分，
report1.stack.txt为提取出来的stacktrace部分(在report1这个示例中没有stacktrace，所以没有创建该文件)，
data/reportsForVSM文件夹下的report1.all.txt为源代码部分和自然语言部分的结合，用于输入到VSM算法下计算相似度。


接下来是第三个版本的算法说明
1. 使用我发送的Indir-5.3-win64-install.exe下载并安装，记下安装目录下的bin目录
2. 将这个项目目录下的Settings.txt改成类似：indripath=C:\Users\82646\Downloads\Indri_5.3\bin\
    等号后面的目录即上步安装目录下的bin目录，记得在bin目录后面加上\符号-不要忘了最后的\符号
3. 调用ArrayList<String> getSimilarTopTenUsingBLUiR(String bugReportPath, String projectPath)算法进行计算
    算法除了名称和以前版本的有区别，其他参数和返回值和之前方法一样。已染色文件都在result/colored_class文件夹下，对bugReport逐行辨别染色后的文件为bugReport同目录下的同名.marked.txt文件，
    如示例中染色后的bugReport为report1.marked.txt。 注意事项也和之前的一样。
    
运行后的生成物多了一些，不过我觉得这个和你这边是透明的，还是提一下：
1. data/reportsForBLUiR目录下是对bugReport脚本化的结果。
2. result/BLUiRRunOut目录下存放的是算法生成的中间产物，包括Index，retrieve的中间结果。



添加gradle项目，然后工具栏里build就可以了。
io.kuy.infozilla.vsm_algorithm.util.FileIOUtil.java 有报错，导入一下包就可以了;
io.kuy.infozilla.cli.Main.java中是使用的示例
主要方法在io.kuy.infozilla.interact_interface.InteractClass
