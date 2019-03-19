import re
import json
from tqdm import tqdm

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
    
    def __init__(self, path):
        self.trace = Trace(path)
        self.patt_str = r'\{"a":.*?[(\])|(\})]\}'
        self.patt = re.compile(self.patt_str)
        self.res = []
        self.call = []
        self.nodes = {}
        self.trees = []
        self.loads()
        
    def loads(self):
        for line in tqdm(self.trace):
            self.res.extend(
                [json.loads(tree) for tree in self.patt.findall(line)]
            )
            
    def parse_nodes(self):
        for node in tqdm(self.res):
            if len(node['p']):
                for parent in node['p']:
                    if not parent in self.nodes.keys():
                        self.nodes[parent] = {
                            "name": "",
                            "time": None,
                            'childs': [node['a']]
                        }
                    else:
                        self.nodes[parent]['childs'].append(node['a'])
                    self.nodes[node['a']] = {
                        "name": node['d'],
                        "time": node['e'] - node['b'],
                        "childs": []
                    }
            else:
                self.call.append(node['a'])
                if node['a'] in self.nodes.keys():
                    self.nodes[node['a']]['name'] = node['d']
                    self.nodes[node['a']]['time'] =  node['e'] - node['b']
                else:
                    self.nodes[node['a']] = {
                        "name": node['d'],
                        "time": node['e'] - node['b'],
                        "childs": []
                    }
                    
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
    vis = []
    for index, child in enumerate(node['childs']):
        if child['name'] in vis:
            continue
        vis.append(child['name'])
        desc['d'] = desc['d'] + '->' + layer + "#" + str(index) + '(' + child['name'] + ')'
        compress(child, desc, deep+1, layer + "#" + str(index))

def hash_tree(trees):
    hashtree = []
    for tree in trees:
        desc = {'d': '0'+'('+tree['name']+')'}
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

