# Style Guide Alpha

Style Guide api for [protocol55.kss](https://github.com/protocol55/kss) and CLI
for generating Style Guides from [KSS documented](https://warpspire.com/kss/syntax/) CSS.
[Example Style Guide](https://protocol55.github.io/styleguide/index.html).

![desktop](https://user-images.githubusercontent.com/1444385/44896306-aa851180-acac-11e8-8839-f347341dea61.png)

![mobile](https://user-images.githubusercontent.com/1444385/44896305-aa851180-acac-11e8-9640-dc57cef4db33.png)

## Installation

### deps.edn

`org.clojars.protocol55/styleguide {:mvn/version "0.1.0"}`

### Leiningen

`[org.clojars.protocol55/styleguide "0.1.0"]`

## Usage

### `protocol55.styleguide.alpha/styleguide`

```
[]
[docs]
  Returns an ordered sequence of sections of the kss documentation maps of docs
  with :reference keys.
```

### `protocol55.styleguide.alpha/add`

```
[styleguide docs]
  Adds the kss documentation maps of docs to styleguide, returning an ordered
  sequence of sections.
```

### `protocol55.styleguide.alpha/sections-seq`

```
[styleguide]
[base styleguide]
[base max-depth styleguide]
[base min-depth max-depth styleguide]
  Returns an ordered sequence of sections of styleguide constrained by the base,
  min-depth and max-depth values.

   base - a vector of parts the reference must contain (default [])
   max-depth - the maximum depth of the references parts (default Integer/MAX_VALUE)
   min-depth - the minimum depth of the references parts (default 0)
```

### `protocol55.styleguide.alpha/get-section-by-reference`

```
[styleguide reference]
  Returns the section containing the :reference key reference or nil if not present.
```

## Reference Conventions

Currently only a couple variations for references are supported. Unless you have
a specific reason not to you should always use named references.

#### Numeric

Numeric references should be separated by a dot, start at 1 and not have zeros
for any of their parts.

Valid numeric references:

- 1
- 1.1
- 200.100.300

Invalid numeric references:

- 0
- 0.1
- 1.0
- 1.0.1

#### Named

Named references should be separated by a dot and use the following characters:
`A-Z a-z _`.

Valid named references:

- Components.Buttons
- Components.Buttons.Primary
- Components.Buttons.Button_Sizes.Small

Invalid named references:

- Components.Buttons.Button Sizes.Small
- Components.Buttons.Button-Sizes.Small

## Custom Ordering

Using the weight property in you KSS documentation will re-order that reference
within the same depth. You will most likely want to do this for your top-level
named references.

## Style Guide CLI

[Example Style Guide](https://protocol55.github.io/styleguide/index.html)

[Example Style Guide Source](https://github.com/protocol55/styleguide/tree/master/resources/public/protocol55/styleguide/content/default_theme)

### Features

- Live reloading of CSS and KSS documentation (via
  [figwheel.main](https://github.com/bhauman/figwheel-main/))
- Responsive default theme
- Can generate a static build for hosting

### Usage

```
Usage: clj -m protocol55.styleguide.main [options]

Options:
  -s, --source DIR        Source directory to recursively parse for KSS comments
  -m, --mask EXT          Extension mask for detecting files containing KSS comments
  -r, --root URI       /  Root uri of the style guide
      --css URL           URL of a CSS file to include in the style guide
      --theme-css URL     URL of a CSS file to include to override the default style guide theme
      --build-dir DIR     Builds an optimized version of the style guide into DIR
      --overview FILE     File name of the overview's Markdown file
  -h, --help
```

### Setup

While using the CLI does not require you to write any Clojure code, you will
need to put any files specified with `--css` or `--theme-css` within a `public`
directory on the classpath. This ensures that the server started by
[figwheel.main](https://github.com/bhauman/figwheel-main/) will serve your
files. The most minimal setup would look like the following:

```
example/
├── resources
│   └── public
│       └── styles.css
└── deps.edn
```

`deps.edn`

```edn
{:deps {org.clojars.protocol55/styleguide {:mvn/version "0.1.0"}}
 :paths ["resources" "target"]}
```

`clj -m protocol55.styleguide.main -s resources/ --css styles.css`

With `styles.css` within the `public` folder in the directory we added to the
classpath via `:paths` we can now specify it using just the relative path it
will be served from, which in this case is `styles.css`. If you had put
`styles.css` inside a nested `css` directory, such as
`resources/public/css/styles.css`, you would instead use `--css css/styles.css`.
Note that you can link to hosted css, such as Google Fonts, by providing the
full url.

We also add `target` to paths. figwheel.main will generate the javascript used
in the style guide here inside a `public` folder, so it should also be on the
classpath.
