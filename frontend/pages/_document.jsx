import Document, { Html, Head, Main, NextScript } from 'next/document'

/** Document racine : charge Material Icons (Google Fonts) et meta responsive. */
export default class MonDocument extends Document {
  render() {
    return (
      <Html lang="fr">
        <Head>
          <link rel="preconnect" href="https://fonts.googleapis.com" />
          <link rel="preconnect" href="https://fonts.gstatic.com" crossOrigin="anonymous" />
          <link
            href="https://fonts.googleapis.com/css2?family=Material+Icons&display=block"
            rel="stylesheet"
          />
          <meta name="theme-color" content="#4f46e5" />
        </Head>
        <body>
          <Main />
          <NextScript />
        </body>
      </Html>
    )
  }
}