#!/bin/bash

vDate=`date +%Y%m%d` 
vHost="172.29.22.244"
vUser=groove
vPasswd=coriant
vFile=YANG.tar.gz
while getopts "w:u:p:h:l:d:" vOption
do
	case ${vOption} in
		w) vWork=$OPTARG ;;
		u) vUser=$OPTARG ;;
		p) vPasswd=$OPTARG ;;
		h) vHost=$OPTARG ;;
		l) vLoadNum=$OPTARG ;;
		d) vDate=$OPTARG ;;
		?) echo "unknown option ${vOption}" ;;
	esac
done
echo "work directory:$vWork  user: ${vUser}  password: ${vPasswd} host: ${vHost} load Number: ${vLoadNum} date: ${vDate} "
[ "x${vLoadNum}" == "x" ] && { echo "usage ftpFile.sh -w work_directory -u user -p password -h host -l load_num -d date"; exit 1; }
[ "x${vWork}" == "x" ] || pushd ${vWork}
pushd yang
test -f ${vFile} && { echo "delete ${vFile} ..."; rm ${vFile}; }
vFullFile="/LoadBuild/DCI${vLoadNum}/GROOVE_G30_${vLoadNum}_${vDate}/${vFile}"
echo "try get from $vHost file $vFullFile"
test -f /usr/bin/expect || { echo "expect must be installed"; exit 1; }
/usr/bin/expect << EOF
spawn scp ${vUser}@${vHost}:${vFullFile} .
expect "password: "
send "${vPasswd}\r\n"
wait 
send_user "\n"
EOF
if [[  -f ${vFile} ]];
then
	echo "download ${vFile} successfully";
else
	echo "no ${vFile} found";
	exit 1;	
fi
echo "==== untar ${vFile}..."
tar zxvf ${vFile}
echo "==== produce data dsdl...."
yang2dsdl -d ../dsdl -t data ne.yang -p ../yang
echo "==== produce config dsdl...."
yang2dsdl -d ../dsdl -t config ne.yang -p ../yang
echo "==== produce get-reply dsdl...."
yang2dsdl -d ../dsdl -t get-reply ne.yang -p ../yang
echo "==== produce get-config-reply dsdl...."
yang2dsdl -d ../dsdl -t get-config-reply ne.yang -p ../yang
echo "==== produce edit-config dsdl...."
yang2dsdl -d ../dsdl -t edit-config ne.yang -p ../yang
echo "==== produce ne notification dsdl...."
yang2dsdl -d ../dsdl -t notification ne.yang -p ../yang
echo "==== produce rpc dsdl...."
yang2dsdl -d ../dsdl -t rpc nbi/coriant-rpc.yang -p ../yang
echo "==== produce rpc-reply dsdl...."
yang2dsdl -d ../dsdl -t rpc-reply nbi/coriant-rpc.yang -p ../yang
echo "==== produce fault notification dsdl...."
yang2dsdl -d ../dsdl -t notification fault/fault-management.yang -p ../yang
popd yang
[ "x${vWork}" == "x" ] || popd ${vWork}

