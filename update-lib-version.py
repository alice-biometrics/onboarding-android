#!/usr/bin/python

import sys

def update_android_version(version):
	print(f"Updating Android version {version}")
	filename = "AliceOnboardingSampleApp/app/build.gradle"

	with open(filename, "r") as file:
		lines = file.readlines()

	for i, line in enumerate(lines):
		if "implementation 'com.alicebiometrics.onboarding:AliceOnboarding:" in line:
			lines[i] = f"\timplementation 'com.alicebiometrics.onboarding:AliceOnboarding:{version}'\n"

	with open(filename, "w") as file:
		file.writelines(lines)

if __name__ == "__main__":
   platform, version = sys.argv[1:]
   update_android_version(version)
