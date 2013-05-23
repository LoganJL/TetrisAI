#include<iostream>
#include<vector>
#include<algorithm>
#include<cmath>

using namespace std;

vector<int> nums;

int main() {
	int n = 0, i;
	while (cin>>i) {
		nums.push_back(i);
		n++;
	}
	cout<<"Got input\n";
	long long sum = 0;
	for (i = 0; i < nums.size(); i++) {
		sum += nums[i];
	}
	cout<<"Sum= "<<sum<<endl;
	sort(nums.begin(), nums.end());
	cout<<"Median= "<<nums[nums.size()/2]<<endl;
	int mean = sum / nums.size();
	cout<<"Mean= "<<mean<<endl;
	int variance = 0;
	for (i = 0; i < nums.size(); i++) {
		variance += (mean - nums[i]) * (mean - nums[i]);
	}
	cout<<"Variance= "<<variance<<endl;
	cout<<"SD= "<<sqrt(variance)<<endl;
}
