def f(maxp):
    p= r"C:\Users\wollknaeul\Documents\HPI\_Semester 8\Information Retrieval\repo\mySearchEngine\res\dewiki-20140216-pages-articles-multistream.xml"
    f = open(p, encoding = "UTF-8")
    f2 = open(p[:-4] + "." + str(maxp) + ".xml", 'w', encoding = "UTF-8")
    pages = 0
    for line in f:
        f2.write(line)
        if "</page>" in line:
            pages += 1
            if pages > maxp:
                break
    f2.write("\n</mediawiki>")
    f2.close()
    
f(10);f(100);f(1000);f(10000);f(100000)
