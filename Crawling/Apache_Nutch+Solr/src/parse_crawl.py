import json
import os

import pandas as pd
import plac


def flush_parsed_text(parsed_text, recno, parsed_dir):
    """Save parsed text to a new file named recno.

    Args
    ----
    parsed_text: list containing lines
    recno: rec id of document
    parsed_dir: directory to store parsed documents in
    """
    if len(parsed_text) == 0:
        return []
    filename = os.path.join(parsed_dir, str(recno).zfill(3))
    with open(filename, 'w', encoding='UTF-8') as fp:
        for line in parsed_text:
            fp.write(line + '\n')
    return []


def flush_metadata(metadata, batch_num, metadata_dir):
    """Flush metadata batch to disk.

    Creates two versions of metadata files - one with outlinks (for calculating pagerank)
    and one without outlinks (this is a csv, to create metadata to be fed to lucene).

    Args
    ----
    metadata: the metadata dictionary
    batch_num: to decide on filename, updated during actual parsing
    metadata_dir: directory to store metadata in
    """
    # with outlinks
    # zero padding, i.e. instead of writing to a file metadata1, uses metadata001
    filename = os.path.join(metadata_dir, 'metadata_with_outlinks_{}.json'.format(str(batch_num).zfill(3)))
    with open(filename, 'w', encoding='UTF-8') as fp:
        json.dump(metadata, fp)

    # without outlinks
    df = (pd.DataFrame.from_dict(metadata, orient='index')
            .reset_index()
            .rename(columns={'index': 'recno'})
            .drop(['outlink'], axis=1))

    # write to csv
    parent_dir = os.path.abspath(os.path.join(metadata_dir, os.pardir))
    df.to_csv(os.path.join(parent_dir, 'metadata_without_outlinks.csv'.format(str(batch_num).zfill(3))),
        encoding='UTF-8', mode='a', index=False)
    return {}


def parse_crawl(crawled_data_file, metadata_dir, parsed_dir, recno, batch_num):
    """Parses the crawled dump and writes metadata and parsed documents to disk.

    Args
    ----
    crawled_data_file: filepath of the crawl dump obtained from nutch
    metadata_dir: directory to store metadata information
    parsed_dir: directory to store parsed documents

    returns:
    latest recno processed
    latest batch number processed
    """

    # create directory if they don't exist
    if not os.path.isdir(metadata_dir):
        os.makedirs(metadata_dir)

    if not os.path.isdir(parsed_dir):
        os.makedirs(parsed_dir)

    current_recno = recno
    batch_size = 1000  # for flushing metadata to disk
    current_batch_num = batch_num
    metadata = {}  # contains url, title, recno, outdegree and num_outlinks
    ix = 0		# no of records seen in this segment
    empty_docs_count = 0
    is_parsed_text = False
    parsed_text = []

    with open(crawled_data_file, 'r', encoding='UTF-8') as fp:

        for line in fp:

            if 'Recno::' in line:
                is_parsed_text = False
                # flush parsed_text
                if not parsed_text:
                	empty_docs_count += 1
                parsed_text = flush_parsed_text(parsed_text, current_recno, parsed_dir)
                ix += 1
                if ix % batch_size == 0:
                    # flush metadata to disk
                    metadata = flush_metadata(metadata, current_batch_num, metadata_dir)  # reset
                    current_batch_num += 1
                current_recno += 1
                if current_recno not in metadata:
                    metadata[current_recno] = {}  # initialize
                else:
                    raise ValueError('Found duplicate recno. Check crawl dump file for errors')

            if 'URL::' in line:
                is_parsed_text = False
                parsed_text = flush_parsed_text(parsed_text, current_recno, parsed_dir)
                url = line.split('::')[1].strip()
                metadata[current_recno]['url'] = url

            if 'Title:' in line:
                title = line.split(':')[1].strip()
                metadata[current_recno]['title'] = title

            if 'Outlinks:' in line:
                outdegree = int(line.split(':')[1].strip())
                metadata[current_recno]['outdegree'] = outdegree

            if 'outlink: toUrl:' in line:
                is_parsed_text = False
                outlink = line.split(': ')[2][:-7].strip()
                if 'outlink' not in metadata[current_recno]:
                    metadata[current_recno]['outlink'] = [outlink]
                else:
                    metadata[current_recno]['outlink'].append(outlink)

            if 'ParseData::' in line:
                is_parsed_text = False
                parsed_text = flush_parsed_text(parsed_text, current_recno, parsed_dir)
            if 'ParseText::' in line:
                is_parsed_text = True
                parsed_text = []

            # add parsed text
            if is_parsed_text and 'ParseText::' not in line:
                stripped_line = line.strip()
                if stripped_line:
                    parsed_text.append(stripped_line)

    # take care of metadata and parsed_text if not flushed
    if metadata:
        flush_metadata(metadata, current_batch_num, metadata_dir)
        current_batch_num += 1

    if parsed_text:
        flush_parsed_text(parsed_text, current_recno, parsed_dir)
        current_recno += 1

    # print stats
    print('Number of recno seen = {}'.format(ix))
    print('Empty documents count = {}'.format(empty_docs_count))
    print('Last recno = {}'.format(current_recno))
    print('Last batch num = {}'.format(current_batch_num))

    return current_recno, current_batch_num


if __name__ == '__main__':
    plac.call(parse_crawl)
