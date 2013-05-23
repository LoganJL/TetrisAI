if [ $# -ne 1 ]
then
	echo "Bad arguments"
	exit 1
fi

while [ true ]
do
	java PlayerSkeleton >> reports/$1.out
	sleep 1
done
