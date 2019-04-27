from flask import Flask, request
import operator
from flask_restful import Resource, Api
import sys
import re
import time
from nltk.stem.porter import *
from nltk.corpus import stopwords
stemmer = PorterStemmer()
stopWords = set(stopwords.words('english'))
dictStopWords = {}
for word in stopWords:
    dictStopWords[word] = 1

app = Flask(__name__)
api = Api(app)

import networkx as nx

def processURLLine(line):
    if len(line) > 0 and line.find('Inlinks:') > 0:
        line=line[0:line.find('Inlinks:')]
        line=line.strip()
    else:
        line = ''
    return line

def processInlinksLine(line):
    if len(line) > 0 and line.find('fromUrl:') > 0 and line.find('anchor:') > 0:
        start = line.find('fromUrl:') + len('fromUrl:')
        end = line.find('anchor:')
        line = line[start:end]
        line=line.strip()
    else:
        line = ''
    return line

def readLinks(f,urlIDs,reverseIDs,inlinks,outlinks,count,parent_ID):
    line=f.readline()
    temp=processInlinksLine(line)
    inlinks.setdefault(parent_ID,[])
    while(len(temp)!=0):
        if temp not in urlIDs:
            urlIDs[temp]=count
            reverseIDs[count]=temp
            count+=1
        curr_id=urlIDs[temp]
        outlinks.setdefault(curr_id,[])
        if parent_ID not in outlinks[curr_id]:
            outlinks[curr_id].append(parent_ID)
        if curr_id not in inlinks[parent_ID]:
            inlinks[parent_ID].append(curr_id)
        line=f.readline()
        temp=processInlinksLine(line)
    return count


def processDump(fileUrl):
    urlIDs={}
    inlinks={}
    outlinks={}
    reverseIDs={}
    f=open(fileUrl,'r')
    count=1
    while True:
        line=f.readline() #read the line
        if not line :break
        temp=processURLLine(line)
        if temp not in urlIDs: #if the url is not in the id mapping, add it to the url mapping
            urlIDs[temp]=count
            reverseIDs[count]=temp
            count+=1
        count=readLinks(f,urlIDs,reverseIDs, inlinks,outlinks,count,urlIDs[temp])
    f.close()
    return inlinks,outlinks,urlIDs,reverseIDs


#urlIDs- array of url ids,outlinks is the dictionary, inlinks is the dictionary
def createGraph(urlIDs,outlinks,inlinks):
    G=nx.DiGraph()
    for id in urlIDs:
        if id in outlinks:
            count=0
            for temp in outlinks[id]:
                G.add_edge(id,temp)
                count+=1
                if count>5:
                    break
        if id in inlinks:
            count=0
            for temp in inlinks[id]:
                G.add_edge(temp,id)
                count+=1
                if count>5:
                    break
    return nx.hits(G,max_iter=100,tol=0.1)

docs = {}
vocab = {}
def readfile(raw_data, docID):
    global docs, vocab
    docs[docID] = {}
    words = modifytext(raw_data).split()
    i = 0
    for word in words:
        try:
            dictStopWords[word]
        except:
            stem = stemmer.stem(word)
            try:
                docs[docID][stem].append(i)
            except:
                docs[docID][stem] = [i]

            try:
                vocab[stem].append(word)
            except:
                vocab[stem] = [word]
            i += 1

def modifytext(s):
    #removing SGML tags
    s = re.sub("\\<.*?>", " ",s)

    #removing digits
    s = re.sub("[\\d+]", " ", s)

    #removing special characters
    #s = re.sub("[+^:,?;=%#&~`$!@*_)/(}{\\.]", " ",s)
    s = re.sub("[^\w]", " ",s)

    #removing possessives
    s = re.sub("\\'s", " ", s)

    #removing "'"
    s = re.sub("\\'", " ", s)

    #removing """
    s = re.sub("\""," ", s)

    #removing "-"
    s = re.sub("-", " ", s)

    #removing whitespaces
    s = re.sub("\\s+", " ", s)

    #all lowercase
    s = s.lower()
    return s

M = {}

def createMatrix():
    global M
    for word in vocab:
        M[word] = {}
        for docID in docs:
            try:
                M[word][docID] = len(docs[docID][word])
            except:
                M[word][docID] = 0

AC = {}
def associationCorrelationFactors():
    global AC
    for word1 in vocab:
        AC[word1] = {}
        for word2 in vocab:
            AC[word1][word2] = 0
            for docID in docs:
                AC[word1][word2] += M[word1][docID]*M[word2][docID]

MC = {}
MS = {}
def metricCorrelationFactors():
    global MC, MS
    words = list(vocab.keys())
    for word in words:
        MC[word] = {}
        MS[word] = {}
    i = 0
    while i<len(words):
        word1 = words[i]
        j = i+1
        while j<len(words):
            word2 = words[j]
            MC[word1][word2] = 0
            for docID in docs:
                try:
                    locs1 = docs[docID][word1]
                    locs2 = docs[docID][word2]
                    for l1 in locs1:
                        for l2 in locs2:
                            MC[word1][word2] += 1.0 / (abs(l1 - l2))
                except:
                    MC[word1][word2] += 0
            MS[word1][word2] = MC[word1][word2] * 1.0/ (len(vocab[word1])+ len(vocab[word2]))
            MS[word2][word1] = MC[word1][word2] * 1.0/ (len(vocab[word1])+ len(vocab[word2]))
            j += 1
        i += 1


def metricClusters():
    global MS
    for word1 in vocab:
        MS[word1] = {}
        for word2 in vocab:
            MS[word1][word2] = MC[word1][word2] * 1.0/ (len(vocab[word1])+ len(vocab[word1]))

AS = {}
def associationClusters():
    global AS
    for word1 in vocab:
        AS[word1] = {}
        for word2 in vocab:
            try:
                AS[word1][word2] = AC[word1][word2] * 1.0/ (AC[word2][word2]+AC[word1][word1]+AC[word1][word2])
            except:
                AS[word1][word2] = 0


SS = {}
def scalarClusters():
    global SS
    for word1 in vocab:
        SS[word1] = {}
        sword1 = []
        for w in vocab:
            sword1.append(AC[word1][w])
        for word2 in vocab:
            sword2 = []
            for w in vocab:
                sword2.append(AC[word2][w])

            s = 0
            i=0
            while i<len(sword1):
                s += sword1[i]*sword2[i]
                i += 1

            SS[word1][word2] = s*1.0/len(sword1)/len(sword2)

def process_Query(query, ch):
    global AS,AC,MS,MC,SS,SC
    AS = {}
    AC = {}
    MS = {}
    MC = {}
    SS = {}
    SC = {}

    res = []
    words = modifytext(query).split()
    if ch=='MC':
        metricCorrelationFactors()
        #metricClusters()
    else:
        associationCorrelationFactors()
        if ch == 'AC':
            associationClusters()
        else:
            scalarClusters()
    for word in words:
        try:
            dictStopWords[word]
        except:
            maxScore = 0
            stem = stemmer.stem(word)
            res.append(stem)
            if ch == 'AC':
                i = 0
                for w in sorted(AS[stem], key=AS[stem].get, reverse=True):
                    if i>1:
                        break
                    res.append(w)
                    i += 1
            elif ch == 'MC':
                i = 0
                for w in sorted(MS[stem], key=MS[stem].get, reverse=True):
                    if i>1:
                        break
                    res.append(w)
                    i += 1
            elif ch == 'SC':
                i = 0
                for w in sorted(SS[stem], key=SS[stem].get, reverse=True):
                    if i>1:
                        break
                    res.append(w)
                    i += 1
    return list(set(res))

'''print "Start"
query = 'easi'
#parser(folder)
print "1"
createMatrix()
print "2"
associationCorrelationFactors()
associationClusters()
process_Query(query, 'AC')
print "3"

metricCorrelationFactors()
metricClusters()
process_Query(query, 'MC')
print "4"

scalarClusters()
process_Query(query, 'SC')

print "Done"'''

class parseURL(Resource):
    #get a list of urls separated by commas
    def get(self,** urls):
        global inlinks,outlinks,urlIDs,reverseIDs
        urls=urls['varargs'].split(',')
        temp_ids=[]

        for url in urls:
            if url in urlIDs:
                temp_ids.append(urlIDs[url])
        h_score,a_score=createGraph(temp_ids,outlinks,inlinks)
        foo={}
        for urlid in temp_ids:
            foo[urlid]=a_score[urlid]
        a_score_new=[]
        sorted_a_score = sorted(foo.items(), key=operator.itemgetter(1),reverse=True) #return  an array of tuples in this format :[(1,1),(2,4)]

        for tupl in sorted_a_score:
            url=reverseIDs[tupl[0]]
            a_score_new.append(url)
        return {'a_score':a_score_new}

    def getKey(self,val,d):
        for key in d.keys():
            if d[key]==val:
                return key

class handleQExp(Resource):
    def post(self):
        jsonObj = request.get_json(force=True)
        query = jsonObj['query']
        resObj = jsonObj['results']
        print(query)
        print(resObj)
        prevtime = time.time()
        i = 1
        for ob in resObj:
            readfile(ob['content'], i)
            i += 1
        stoptime = time.time()
        print(stoptime-prevtime)
        prevtime = stoptime
        createMatrix()
        stoptime = time.time()
        print(stoptime-prevtime)
        prevtime = stoptime
        expandedQuery = ' '.join(process_Query(query, 'MC'))
        stoptime = time.time()
        print(stoptime-prevtime)
        prevtime = stoptime
        print('Expndded Query..')
        print(expandedQuery)
        return {'newQuery':expandedQuery}


api.add_resource(parseURL, '/getHITS/<path:varargs>')
api.add_resource(handleQExp, '/getQueryExp/')

if __name__=='__main__':
    inlinks,outlinks,urlIDs,reverseIDs=processDump(sys.argv[1])
    print("Server started...")
    app.run()
