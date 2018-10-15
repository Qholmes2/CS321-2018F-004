#! /bin/sh
rmiregistry &; java -cp ".;../lib/*" -Djava.security.policy=game.policy GameServer localhost
