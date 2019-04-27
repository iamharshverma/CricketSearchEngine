import glob
import re
import json
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
from sklearn.metrics import adjusted_rand_score
from sklearn.cluster import AgglomerativeClustering
documents=[]
url_name=[]

dir_path ='/home/vatsal/Desktop/Academics/Spring2019/CS6322_S19/FInalProject/IR/IR/crawling/apache-nutch-1.15/crawl/dump_dir_op/*'
files = glob.glob(dir_path)
readDoc=0
urldict={}
for file in files:
    if file.endswith('.json'):
        myfile= open(file,'r')
        data=myfile.read()
        obj = json.loads(data)
        res = dict((v,k) for k,v in obj.items())
       # for m,n in res.items():
        #    print(m,n)
for k,v in res.items():
    #print(v)
    readDoc=readDoc+1
    if v.endswith('.html'):
        txt = open(v,"r",encoding='utf-8', errors='ignore').read().lower()
        a= txt.replace('\n',' ')
        rem_tags = re.compile('<.*?>')#removing html tags
        text_one =re.sub(rem_tags, '', txt)
        text_two = re.sub(r'[^\w\s]','',text_one)#removing punctuations
        url_name.append(k)
        documents.append(text_two)
        #print(documents)
#print("a")
vectorizer = TfidfVectorizer(stop_words='english', use_idf=True, smooth_idf=True, sublinear_tf=True)
X = vectorizer.fit_transform(documents)
print(X.toarray())

true_k = 10

model = AgglomerativeClustering(n_clusters=true_k,affinity='cosine',linkage='single')
agglo =model.fit(X.toarray())
#print(agglo.labels_)

with open("averageagglo.txt","w") as f:
    for i in range(len(url_name)):
        #print(url_name[i]," cluster: ",agglo.labels_[i])
        f.write(str(url_name[i]) + ": "+ str(agglo.labels_[i])+'\n')

