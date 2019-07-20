# insectarium

> An insectarium is a live bug zoo, or a museum or exhibit of live bugs.

This is a native desktop GUI for bug tracking.

Many of the web interfaces are slow and clunky, while the APIs
supporting them are fast.  This aims to be a streamlined way to
work your day to day tickets, without all the clutter and distraction
the web interfaces thrust on our lives.

<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-refresh-toc -->
**Table of Contents**

- [insectarium](#insectarium)
- [Currently supported/being added](#currently-supportedbeing-added)
- [Setup](#setup)
    - [Customizing your setup](#customizing-your-setup)
        - [For Jira](#for-jira)
        - [For Github](#for-github)
- [TODO](#todo)
- [License](#license)

<!-- markdown-toc end -->

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

## Customizing your setup

Please see `conf/default-rc` as your starting point.

Each available provider will have a top level key to define their
config (I may add simultaneous provider support soon, so you can view
tickets from many sources in one centralized area).

You can choose which provider by setting the key appropriately.

You can add your own settings and keep them out of this repository
directory by copying the defaults to your
`XDG_CONFIG_HOME/insectarium/insectariumrc` or `~/.insectariumrc`
files.

### For Jira

See the file `conf/default-rc` for detailed setup instructions.

You will need to ensure you have some type of authentication (a jira
API token, or, much easier - just copy your Jira cookie out of a
browser session and put in a file Insectarium can load, such as:
`/tmp/insectarium-jira-token.txt`), this will work in all Jira use
cases and is in general easier to set up / prepare than the token
based flow (but note, it would be less secure if this file were
compromised in some way, so use at your own risk).

If you choose to, the file would end up looking similar to this:

```
cloud.session.token=eyJ...<many characteres of token>...mdQ
```

Afterwords, you can run:

```
lein run
```

Basic auth instructions here:

https://confluence.atlassian.com/cloud/api-tokens-938839638.html

### For Github

See the file `conf/default-rc` for detailed setup instructions.

You can also use a personal access token instead of the password:

https://github.blog/2013-05-16-personal-api-tokens/

Afterwords, you can run:

```
lein run
```

# TODO

- Add a docker based setup

# License

Copyright © 2019 Matthew Carter <m@ahungry.com>

Distributed under the GNU General Public License either version 3.0 or (at
your option) any later version.
