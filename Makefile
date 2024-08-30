.PHONY: bump-major bump-minor bump-patch

bump-major:
	@./bump-version.sh major

bump-minor:
	@./bump-version.sh minor