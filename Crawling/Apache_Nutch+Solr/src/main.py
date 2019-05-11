import os
import pandas as pd
import plac

from parse_crawl import parse_crawl
from compute_pagerank import compute_pagerank


def create_metadata(metadata_file, pagerank_file):
	"""Creates metadata for lucene, join pagerank scores with metadata (title, url, recno).

	Has NaN values in final file.
	"""
	parent_dir = os.path.abspath(os.path.join(pagerank_file, os.pardir))
	metadata_without_outlinks_file = os.path.join(parent_dir, 'metadata_without_outlinks.csv')
	metadata = pd.read_csv(metadata_without_outlinks_file)
	pagerank = pd.read_csv(pagerank_file)
	df = pd.merge(metadata, pagerank, how='left', on=['url'])
	df.to_csv(metadata_file, index=False, encoding='UTF-8')


def parse_segments(document_dump_dir, metadata_folder_name, parsed_folder_name):
	"""Parse all segments."""
	folders = os.listdir(document_dump_dir)
	recno = -1
	batch_num = 0
	for folder in folders:
		recno, batch_num = parse_crawl(
			os.path.join(document_dump_dir, folder, 'dump'),
			metadata_folder_name,
			parsed_folder_name,
			recno,
			batch_num)


def main(document_dump_dir,
	metadata_folder_name,
	parsed_folder_name,
	pagerank_filename,
	metadata_filename):
	"""Main function to convert raw dump from nutch to a format that indexer expects.

	Args
	----
	document_dump_dir: folder that contains raw dump across segments from nutch
	metadata_folder_name: store metadata for pagerank calculation in this folder
	parsed_folder_name: folder to store parsed documents
	pagerank_filename: file that will contain pagerank scores for crawled urls
	metadata_filename: final metadata file that will contain url, title, recno, outdegree and pagerank scores 
	"""
	parse_segments(document_dump_dir, metadata_folder_name, parsed_folder_name)
	compute_pagerank(metadata_folder_name, pagerank_filename)
	create_metadata(metadata_filename, pagerank_filename)


if __name__ == '__main__':
	plac.call(main)