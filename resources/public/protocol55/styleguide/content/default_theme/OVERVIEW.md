# Style Guide Generator

This is the Style Guide for the default theme of the
[Protocol55 Style Guide generator](https://github.com/protocol55/styleguide).

## What is a Style Guide?

Whether you know it or not as soon as you begin designing your UI you've started
to create a Design System. A Design System is all the choices around why certain
patterns and visual styles are used how they are.

A Style Guide is the representation of your Design System. It's the place to
document the "why", and let others know what is available.

Some examples of Style Guides:

- [GitHub Primer](https://styleguide.github.com/primer/)
- [Yelp Style Guide](https://www.yelp.com/styleguide)
- [MailChimp Pattern Library](http://ux.mailchimp.com/patterns)
- [Heroku Purple](http://purple.herokuapp.com/)

## Living Style Guides

A living Style Guide is one that is generated from your code. The Protocol55
Style Guide generator parses [KSS documentation](https://warpspire.com/kss/syntax/)
found in the source files of you CSS and turns this into a Style Guide you can
distribute to your team.

## Generating

The Protocol55 Style Guide generator is based off a couple existing projects:

- [kss-node](https://github.com/kss-node/kss-node)
- [SC5 Style Guide Generator](https://github.com/SC5/sc5-styleguide)

The above projects both require you to install them via npm and run them via
node.

The Protocol55 Style Guide generator only requires the installation of the `clj`
command line tool for you to start using it. The most basic command to get
started is:

```bash
clj -m protocol55.styleguide.main -s resources/ --css css/styles.css
```

That assumes that your documented CSS lives under `resources/` and that
`css/styles.css` is in a `public/` directory on the classpath.

The above command will launch a [fighweel.main](https://github.com/bhauman/figwheel-main/)
session serving the Style Guide at `localhost:9500`. It will parse and
live-reload your KSS documentation as well as any included `--css`. See the
project [README](https://github.com/protocol55/styleguide) for more details on setup and options.

When you're ready to host your Style Guide for others to see you can append a
`--build-dir dist/` option to create an optimized build.

## Running Locally

To run this from a cloned version of the repo use: `clj -Atheme`
