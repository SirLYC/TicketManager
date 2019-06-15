import json
import os
import re
import socket
import time
import urllib
import urllib.error
import urllib.parse
import urllib.request

timeout = 5
socket.setdefaulttimeout(timeout)
image_suffixes = ['.bmp', '.jpg', '.png', '.jpeg']


class Crawler:
    __time_sleep = 0.1
    __amount = 0
    __start_amount = 0
    __counter = 0
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0'}

    # t download interval
    def __init__(self, t=0.1):
        self.time_sleep = t

    @staticmethod
    def get_suffix(name):
        m = re.search(r'\.[^.]*$', name)
        if m.group(0) and len(m.group(0)) <= 5:
            return m.group(0)
        else:
            return '.jpeg'

    @staticmethod
    def get_referrer(url):
        par = urllib.parse.urlparse(url)
        if par.scheme:
            return par.scheme + '://' + par.netloc
        else:
            return par.netloc

    # save image
    def save_image(self, rsp_data, word):
        dir_name = os.path.join(".", "craw_downloaded", word)
        if not os.path.exists(dir_name):
            os.makedirs(dir_name)

        success_cnt = 0
        image_infos = rsp_data['imgs']
        total_len = len(image_infos)
        with open(os.path.join(dir_name, "urls.txt"), "a") as url_record_file:
            # avoid repeat
            self.__counter = len(os.listdir(dir_name)) + 1
            for image_info in image_infos:
                url = str(image_info['objURL'])
                print("----download " + url + " -> ", end='')
                try:
                    if not url or url.isspace() or not url.startswith("http"):
                        raise Exception("not http or https: " + url)

                    time.sleep(self.time_sleep)
                    suffix = self.get_suffix(image_info['objURL'])
                    if suffix not in image_suffixes:
                        raise Exception("skip gif")
                    # avoid 403
                    # refer = self.get_referrer(image_info['objURL'])
                    opener = urllib.request.build_opener()
                    # get image that won't be 403
                    # opener.addheaders = [
                    #     ('User-agent', 'Mozilla/5.0 (Windows NT 10.0; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0'),
                    #     ('Referer', refer)
                    # ]
                    urllib.request.install_opener(opener)

                    # save image
                    urllib.request.urlretrieve(url,
                                               os.path.join(dir_name, ''.join([str(self.__counter), str(suffix)])))
                    # success
                    # save url to file
                    url_record_file.write(url + os.linesep)
                    success_cnt += 1
                except urllib.error.HTTPError as urllib_err:
                    print("fail: {}".format(urllib_err))
                    continue
                except Exception as err:
                    print("fail: {}".format(err))
                    continue
                else:
                    print("ok.")
                    self.__counter += 1
        return success_cnt, total_len

    # start
    def get_images(self, word):
        search = urllib.parse.quote(word)
        # pn int image count
        pn = self.__start_amount
        page = None
        cnt = 0
        total_len = 0
        while pn < self.__amount:
            url = 'http://image.baidu.com/search/avatarjson?tn=resultjsonavatarnew&ie=utf-8&word=' + search + '&cg=girl&pn=' + str(
                pn) + '&rn=60&itg=0&z=0&fr=&width=&height=&lm=-1&ic=0&s=0&st=-1&gsm=1e0000001e'
            try:
                time.sleep(self.time_sleep)
                req = urllib.request.Request(url=url, headers=self.headers)
                page = urllib.request.urlopen(req)
                rsp = page.read().decode('unicode_escape')
            except UnicodeDecodeError as e:
                print(e)
                print('-----UnicodeDecodeErrorurl:', url)
            except urllib.error.URLError as e:
                print(e)
                print("-----urlErrorurl:", url)
            except socket.timeout as e:
                print(e)
                print("-----socket timout:", url)
            else:
                # parse json
                rsp_data = json.loads(rsp)
                (c, t) = self.save_image(rsp_data, word)
                cnt += c
                total_len += t
                # next page
                pn += 60
            finally:
                if page:
                    page.close()
        print("----finish. success: {}, fail: {}----".format(cnt, total_len - cnt))
        return

    def start(self, word, spider_page_num=1, start_page=1):
        """
        start to fetch image
        :param word: keyword
        :param spider_page_num: page count, total = page * 60
        :param start_page: start page
        :return:
        """
        self.__start_amount = (start_page - 1) * 60
        self.__amount = spider_page_num * 60 + self.__start_amount
        self.get_images(word)


if __name__ == '__main__':
    crawler = Crawler(0.05)
    crawler.start('漫威所有英雄高清壁纸', 8, 1)  # 8 * 60 = 480 pics
