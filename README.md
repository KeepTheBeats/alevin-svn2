# 改进：在用alevin运行DPVNE算法时，用xml导入拓扑，并循环50次分别计算50、100、150、……、2500个请求的情况。

1. 用alevin运行DPVNE算法时，DPVNE算法与其它算法不同，并不能在alevin的main程序打开的图形界面中选择到该算法，DPVNE算法有着单独的程序。
2. 在DPVNE程序的main方法中，调用AlgorithmEvaluation类（对象）的executeTests方法,进支DPVNE算法的计算。
3. 在DPVNE算法那部分程序中，原本没有通过xml文件输入底层网络、虚拟网络、需求的功能。
4. 我通过调用XMLImporter.importScenario("xxx.xml")从描述scenario的xml文件中得到一个scenario对象，再通过scenario对象（类）的getNetworkStack()方法从scenario中得到一个NetworkStack对象，再通过NetworkStack对象（类）的getSubstrate方法获得一个SubstrageNetwork对象（底层网络），通过NetworkStack对象（类）的getVirtuals方法获得一个List<VirtualNetwork>对象，并转换为LinkedList<VirtualNetwork>对象。
5. 在AlgorithmEvaluation类的executeTests方法中，调用algorithm.map()方法时，把从xml中获取到的SubstrageNetwork对象和LinkedList<VirtualNetwork>对象做为algorithm.map()的参数。
6. 这样就用DPVNE算法对该xml文件中的情景进行了计算。
