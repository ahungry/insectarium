# insectarium

> An insectarium is a live bug zoo, or a museum or exhibit of live bugs.

This is a GUI for bug tracking.

![insectarium](https://github.com/ahungry/insectarium/blob/master/insectarium.png)

# Currently supported/being added

- Jira Tickets
- Github Issues

# Setup

If using Arch Linux, ensure you are using java 12 which has openjfx,
or it will not work with the supporting lib (cljfx).

```
sudo archlinux-java set java-12-openjdk
```

Then you can install deps and run as follows:

```
cd gui
lein deps
```

## Run for jira

Then you can run against Jira as such (make sure to add some type of
jira authentication, such as a jira cookie into /tmp/token.txt):

```
lein run "jira" "https://<your jira here>.atlassian.net"
```

## Run for github

Then you can run against Github as such (make sure to add some type of
github authentication, such as a github user:password for basic auth, into /tmp/token.txt):

```
lein run "github"
```

# TODO

- Add proper authentication (OAuth support) for the various providers

# License

Copyright © 2019 Matthew Carter <m@ahungry.com>

Distributed under the GNU General Public License either version 3.0 or (at
your option) any later version.
