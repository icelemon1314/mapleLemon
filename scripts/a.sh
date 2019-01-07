#!/bin/bash
#格式：./gb2312_2_utf8.sh 路径名

IFSBACKUP=$IFS							#备份IFS变量
IFS=$(echo -en "\n\b")						#设置IFS变量不含空格，防止文件名中有空格时出现异常

dst=$1					#如果路径末尾有“/”，删除掉，后面再添加
for file in $(ls "$dst"|grep .js)
do
  gb2312file=$dst\/$file
  utf8file=$(echo "$gb2312file"|sed 's/.js$/-utf8.js/')
  string=$(file "$gb2312file"|grep Unicode)			#简单判断文件是否为Unicode文件，如果是，则不转换
  if [ "$string" = "" ]
  then
    iconv -f GB18030 -t utf-8 "$gb2312file" > "$utf8file"	#GB18030编码，它是GB2312的一个超集
    cp "$utf8file" "$gb2312file"
    rm -f "$utf8file"
  else
    echo "$gb2312file" is Unicode text file
  fi
done

IFS=$IFSBACKUP

exit 0
