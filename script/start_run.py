import argparse
import os
import platform

import pymysql

pass_mac = 'xxxx'
pass_prod = 'xxxx'
pass_win = 'xxxx'


def choose_env():
    sys_str = platform.system()
    if sys_str == "Windows":
        return sys_str, pass_win, False
    elif sys_str == "Linux":
        return sys_str, pass_prod, True
    else:
        return sys_str, pass_mac, False


def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument("-d", "--drop", help="drop last database", action="store_true")
    args = parser.parse_args()
    return args


def drop_old_data(password):
    db = pymysql.connect("localhost", "root", password,
                         charset='utf8mb4',

                         cursorclass=pymysql.cursors.DictCursor)
    db.query('drop database if exists ticket_manager')
    db.query('create database if not exists ticket_manager')
    return True


def main():
    args = parse_args()
    drop_data = args.drop
    if drop_data:
        while True:
            print("Warning: this action will drop old database!\nContinue? [Y/N]:", end=' ')
            user_input = str(input()).upper()
            if user_input == 'YES' or user_input == 'Y':
                break
            elif user_input == 'NO' or user_input == 'N':
                print("drop.")
                return
    system_name, password, in_prod = choose_env()
    print("current system: {}, is in prod: {}".format(system_name, in_prod))

    exec_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    os.chdir(exec_dir)

    if drop_data and not drop_old_data(password):
        print("cannot execute drop data.")
        return

    if in_prod:
        # kill running server
        os.system('jps | grep TicketManager | cut -f1 -d" " | xargs kill -9')
        if os.system('gradle bootJar') != 0:
            print("build failed.")
            return
        if os.system('nohup java -jar ./build/libs/TicketManager_Backend-1.0.0.jar urls.txt >log.log 2>err.log &') != 0:
            print("start server failed")
            return
        print("ok!")
    else:
        os.system('./gradlew bootRun')


if __name__ == '__main__':
    main()
