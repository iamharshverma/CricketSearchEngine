import itertools
import json
import os

import networkx as nx
import pandas as pd
import plac


def get_all_crawled_urls(metadata_dir):
    """Get set of urls of all non-empty documents across all segments."""
    metadata_files = [os.path.join(metadata_dir, file) for file in os.listdir(metadata_dir)]
    urls = set()
    for file in metadata_files:
        with open(file, 'r', encoding='UTF-8') as fp:
            data = json.load(fp)
            for recno in data:
                url = data[recno]['url']
                urls.add(url)
    print('Total {} crawled urls'.format(len(urls)))
    return urls


def update_graph_from_outlinks(G, metadata_file, crawled_urls):
    """Read outlinks from metadata file and update graph."""
    count_log = []
    with open(metadata_file, 'r', encoding='UTF-8') as fp:
        data = json.load(fp)
        for recno in data:
            url = data[recno]['url']
            outlinks = data[recno].get('outlink', None)
            if outlinks:
                crawled_outlinks = set(outlinks).intersection(crawled_urls)
                edges = list(itertools.product([url], list(crawled_outlinks)))
                count_log.append((len(outlinks), len(edges)))   # to notice difference in crawled outlinks and actual
                G.add_edges_from(edges)
    print('{} ==> {}'.format(metadata_file, count_log))
    return G


def create_webgraph(metadata_dir):
    """Compute pagerank using all metadata."""
    metadata_files = [os.path.join(metadata_dir, file) for file in os.listdir(metadata_dir)]
    crawled_urls = get_all_crawled_urls(metadata_dir)
    G = nx.DiGraph()  # initialize a directed graph
    for file in metadata_files:
        print('Processed', file)
        G = update_graph_from_outlinks(G, file, crawled_urls)
    return G


def compute_pagerank(metadata_dir, outfile):
    """Compute pageranks using metadata extracted from crawled data and writes to a csv file."""
    G = create_webgraph(metadata_dir)
    pagerank = pd.DataFrame.from_dict(nx.pagerank(G), orient='index').reset_index()
    pagerank.columns = ['url', 'pagerank']
    pagerank.to_csv(outfile, index=False)


if __name__ == '__main__':
    plac.call(compute_pagerank)
