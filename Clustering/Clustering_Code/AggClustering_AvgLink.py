from flask import Flask, request
import operator
from flask_restful import Resource, Api
import sys
import re
import time
from collections import defaultdict, OrderedDict

app = Flask(__name__)
api = Api(app)

import networkx as nx

clusters = {}

def readClusters():
    filename = '/Users/harshverma/Downloads/IR_Final/IR/crawling/Clustering/kmeansFinalAvg.txt'
    with open(filename) as f:
        for line in f:
            print(line)
            lis = line.split(' cluster: ')
            clusters[lis[0]] = lis[1]
    return

def extractURLAndJsonObj(jsonObj):
    urlDict = OrderedDict()
    for obj in jsonObj:
        urlDict[obj['id']] = obj
    return urlDict

def findMaxCluster(urls):
    print("URLS",urls)
    counter = defaultdict(int)
    print("ALL CLUSTERS ARE:",clusters)
    for i in range(len(urls)):
        if i == 15:
            break
        if urls[i] in clusters:
            print("URLS" , urls[i])
            print("Clusters" ,clusters)
            clust = clusters[urls[i]]
            counter[clust] += 1
    return max(counter, key=counter.get)


def getFinalJsonObjs(maxFreqCluster, urlDict):
    result = []
    count = 0
    print(("Chosen max cluster as :{}").format(maxFreqCluster))
    for k,v in urlDict.items():
        if count == 15:
            break
        if k in clusters and clusters[k] == maxFreqCluster:
            result.append(v)
            count += 1
    print(("Chosen {} from top 15").format(count))
    if count < 15:
        for k,v in urlDict.items():
            if count == 15:
                break
            if k in clusters and clusters[k] != maxFreqCluster:
                result.append(v)
                count += 1
    return result


class handleClustering(Resource):
    def post(self):
        jsonObj = request.get_json(force=True)
        resObj = jsonObj['results']
        urlDict = extractURLAndJsonObj(resObj)
        maxFreqCluster = findMaxCluster(list(urlDict.keys()))
        finalObjs = getFinalJsonObjs(maxFreqCluster, urlDict)
        return {"results":finalObjs}

api.add_resource(handleClustering, '/getAgglomerativeClusteringResults/')

if __name__=='__main__':
    print("Clustering Server started...")
    readClusters()
    app.run(port=5004)
