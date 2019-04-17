import re
import json
from tqdm import tqdm
import pandas as pd
from collections import defaultdict


def produce():
    return {
        'name': None,
        'time': None,
        'begin': None,
        'end': None,
        'childs': []
    }


class Trace:
    
    def __init__(self, path):
        self.path = path
        
    def __iter__(self):
        f = open(self.path)
        while True:
            line = f.readline()
            if line:
                yield line
            else:
                f.close()
                return


class Parse:
    
    def __init__(self, paths):
        self.traces = [Trace(path) for path in paths]
        self.patt_str = r'\{"a":.*?[(\])|(\})]\}'
        self.patt = re.compile(self.patt_str)
        self.res = []
        self.call = []
        self.nodes = defaultdict(produce)
        self.trees = []
        self.loads()
        
    def loads(self):
        for trace in tqdm(self.traces):
            for line in trace:
                self.res.extend(
                    [json.loads(tree) for tree in self.patt.findall(line)]
                )

    def parse_nodes(self):
        for func in self.res:
            cur = self.nodes[func['a']]
            if not cur['name']:
                cur['name'] = func['d']
                cur['time'] = func['e'] - func['b']
                cur['begin'] = func['b']
                cur['end'] = func['e']

            if not len(func['p']):
                self.call.append(func['a'])
                continue

            for p_hash in func['p']:
                parent = self.nodes[p_hash]
                parent['childs'].append(func['a'])

    def build_tree(self):
        
        if not len(self.call):
            self.parse_nodes()
            
        def deepCall(root, callTree):
            if not len(self.nodes[root]['childs']):
                return
            for child in self.nodes[root]['childs']:
                callTree['childs'].append({
                    "hash": child,
                    "name": self.nodes[child]['name'],
                    "childs": []
                })
                deepCall(child, callTree['childs'][-1])

        for root in tqdm(self.call):
            self.trees.append({
                "hash": root,
                "name": self.nodes[root]['name'],
                "childs": []
            })
            deepCall(root, self.trees[-1])


def compress(node, desc, deep, layer):
    layer = layer + "&" + str(deep)
    # vis = set()
    for index, child in enumerate(node['childs']):
        name = child['name']
        if '[' in child['name']:
            name = child['name'][:child['name'].find('[')]
        if '(' in child['name']:
            name = child['name'][:child['name'].find('(')]
        # if name in vis:
        #     continue
        # vis.add(name)
        desc['d'] = desc['d'] + '->' + layer + "#" + str(index) + '(' + name + ')'
        compress(child, desc, deep+1, layer + "#" + str(index))


def hash_tree(trees):
    hashtree = []
    for tree in trees:
        name = tree['name']
        if '[' in tree['name']:
            name = tree['name'][:tree['name'].find('[')]
        if '(' in tree['name']:
            name = tree['name'][:tree['name'].find('(')]
        desc = {'d': '0'+'('+name+')'}
        compress(tree, desc, 1, '0')
        hashtree.append(desc['d'])
    return hashtree


def clean_nodes(data):
    name, time, child = [],[],[]
    for d in data:
        name.append(d['name'])
        time.append(d['time'])
        child.append(len(d['childs']))
    func_info = {}
    func_info['name'] = name
    func_info['time'] = time
    func_info['child'] = child
    return func_info


def read_trace(file, flag=False):

    pfile = file.split('.')[0]+'.'+'pkl'
    if flag:
        n = pd.read_pickle(pfile)
        return n
    wordcount = Parse(file)
    wordcount.parse_nodes()
    root_nodes = [wordcount.nodes[hash_node] for hash_node in wordcount.call]
    nodes_info = {
        'name': [],
        'time': [],
        'begin': [],
        'end': []
    }
    for root in root_nodes:
        nodes_info['name'].append(root['name'])
        nodes_info['time'].append(root['time'])
        nodes_info['begin'].append(root['begin'])
        nodes_info['end'].append(root['end'])
    n = pd.DataFrame(nodes_info)
    n.to_pickle(pfile)
    return n
