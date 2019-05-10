import glob
import re
import json
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.cluster import KMeans
from sklearn.metrics import adjusted_rand_score
from sklearn.cluster import AgglomerativeClustering
documents=[]
url_name=[]
res={}
resGlobal={}
dir_path ='/Users/harshverma/Downloads/IR_Vatsal/IR/crawling/apache-nutch-1.15/crawl/dump_dir_op/*'
files = glob.glob(dir_path)
readDoc=0
urldict={}

for file in files:
    if file.endswith('.json'):
        myfile= open(file,'r')
        data=myfile.read()
        obj = json.loads(data)
        res = dict((v,k) for k,v in obj.items())
    resGlobal.update(res)
print(len(res))
print(len(resGlobal))
for k,v in resGlobal.items():
   # print(v)
    readDoc=readDoc+1
    if v.endswith('.html'):
        txt = open(v,"r", encoding='utf-8', errors='ignore').read().lower()
        a= txt.replace('\n',' ')
        rem_tags = re.compile('<.*?>')#removing html tags
        text_one =re.sub(rem_tags, '', txt)
        text_two = re.sub(r'[^\w\s]','',text_one)#removing punctuations
        url_name.append(k)
        documents.append(text_two)
        #print(documents)
print(len(url_name))
vectorizer = TfidfVectorizer(stop_words='english')
X = vectorizer.fit_transform(documents)
true_k = 15
model = KMeans(n_clusters=true_k, init='k-means++', max_iter=100, n_init=1)
kmean =model.fit(X)
print("HIIIIIII")
with open("kmeansFinal.txt","w") as f:
    for i in range(len(url_name)):
     #   print(url_name[i]," cluster: ",kmean.labels_[i])
        f.write(str(url_name[i]) + " cluster: "+ str(kmean.labels_[i])+'\n')
