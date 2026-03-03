import React from "react";

const HeadMeta: React.FC = () => {
    return (
        <>
            <meta httpEquiv="Content-Security-Policy" content="default-src 'self' 'unsafe-inline' 'unsafe-eval' data:; connect-src 'self' https://compchem17.ipb-halle.de;   frame-src 'self' blob:;"
            />
            <meta name="apple-mobile-web-app-status-bar-style" content="black" />
            <meta name="apple-mobile-web-app-capable" content="yes" />
            {/* Apple Icons */}
            <link rel="apple-touch-icon" sizes="57x57" href="/assets/img/systemIcons/apple-icon-57x57.png" />
            <link rel="apple-touch-icon" sizes="60x60" href="/assets/img/systemIcons/apple-icon-60x60.png" />
            <link rel="apple-touch-icon" sizes="72x72" href="/assets/img/systemIcons/apple-icon-72x72.png" />
            <link rel="apple-touch-icon" sizes="76x76" href="/assets/img/systemIcons/apple-icon-76x76.png" />
            <link rel="apple-touch-icon" sizes="114x114" href="/assets/img/systemIcons/apple-icon-114x114.png" />
            <link rel="apple-touch-icon" sizes="120x120" href="/assets/img/systemIcons/apple-icon-120x120.png" />
            <link rel="apple-touch-icon" sizes="144x144" href="/assets/img/systemIcons/apple-icon-144x144.png" />
            <link rel="apple-touch-icon" sizes="152x152" href="/assets/img/systemIcons/apple-icon-152x152.png" />
            <link rel="apple-touch-icon" sizes="180x180" href="/assets/img/systemIcons/apple-icon-180x180.png" />
            {/* Android Icons */}
            <link rel="icon" type="image/png" sizes="192x192" href="/assets/img/systemIcons/android-icon-192x192.png" />
            <link rel="icon" type="image/png" sizes="32x32" href="/assets/img/systemIcons/favicon-32x32.png" />
            <link rel="icon" type="image/png" sizes="96x96" href="/assets/img/systemIcons/favicon-96x96.png" />
            <link rel="icon" type="image/png" sizes="16x16" href="/assets/img/systemIcons/favicon-16x16.png" />
            <link rel="manifest" href="/assets/img/systemIcons/manifest.json" />
            <meta name="msapplication-TileColor" content="#ffffff" />
            <meta name="theme-color" content="#ffffff" />
        </>
    );
};

export default HeadMeta;